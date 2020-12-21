package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;

/**
 * desc :
 * name : ConsumerThread
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class ConsumerRunner extends Thread {

    final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);

    private ConsumerService consumerService;

    private CDCMetricsService cdcMetricsService;

    private CDCConnector cdcConnector;

    private static volatile RunningStatus runningStatus;

    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsService cdcMetricsService,
                          CDCConnector cdcConnector) {

        this.consumerService = consumerService;
        this.cdcMetricsService = cdcMetricsService;
        this.cdcConnector = cdcConnector;
    }

    public void shutdown() {
        runningStatus = RunningStatus.TRY_STOP;
        cdcMetricsService.shutdown();

        int useTime = 0;
        while (useTime < MAX_STOP_WAIT_LOOPS) {
            try {
                Thread.sleep(MAX_STOP_WAIT_TIME * SECOND);
            } catch (Exception e) {
                //  ignore
                logger.warn("[cdc-runner] shutdown error, message : {}", e.getMessage());
            }

            if (isShutdown()) {
                logger.info("[cdc-runner] consumer success stop.");
                break;
            }

            useTime++;
        }

        if (useTime >= MAX_STOP_WAIT_LOOPS) {
            logger.warn("[cdc-runner] force stop after {} seconds.", useTime * MAX_STOP_WAIT_TIME);
        }
    }

    public boolean isShutdown() {
        return runningStatus.equals(RunningStatus.STOP_SUCCESS);
    }

    public void run() {
        runningStatus = RunningStatus.INIT;

        cdcMetricsService.startMetrics();

        while (true) {
            //  判断当前服务状态是否可运行
            if (checkForStop()) {
                break;
            }

            try {
                //  连接CanalServer，如果是服务启动(runningStatus = INIT),则同步缓存中cdcMetrics信息
                connectAndReset();
            } catch (Exception e) {
                closeToNextReconnect(CDCStatus.DIS_CONNECTED,
                        String.format("[cdc-runner] %s, %s", "canal-server connection error", e.getMessage()));
                continue;
            }
            //  连接成功，重置标志位
            runningStatus = RunningStatus.RUN;

            try {
                //  开始消费
                consume();
            } catch (Exception e) {
                closeToNextReconnect(CDCStatus.CONSUME_FAILED,
                        String.format("[cdc-runner] %s, %s", "canal-client consume error", e.getMessage()));
            }
        }
    }

    private void connectAndReset() throws SQLException {

        cdcConnector.open();

        //  首先将上次记录完整的信息(batchID)确认到Canal中
        syncAndRecover();
    }

    private void closeToNextReconnect(CDCStatus cdcStatus, String errorMessage) {
        cdcConnector.close();
        logger.error(errorMessage);

        //  这里将进行睡眠->同步错误信息->进入下次循环
        callBackError(RECONNECT_WAIT_IN_SECONDS * SECOND, cdcStatus);
    }

    private boolean checkForStop() {
        if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
            cdcConnector.shutdown();
            runningStatus = RunningStatus.STOP_SUCCESS;
            return true;
        }
        return false;
    }

    public void consume() throws SQLException {
        StopWatch getMessageWatch = new StopWatch("getMessage");
        while (true) {
            //  服务被终止
            if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
                runningStatus = RunningStatus.STOP_SUCCESS;
                break;
            }

            Message message = null;
            long batchId;
            try {
                getMessageWatch.start();
                //获取指定数量的数据
                message = cdcConnector.getMessageWithoutAck();
                getMessageWatch.stop();

                batchId = message.getId();

                if (getMessageWatch.getLastTaskTimeMillis() > MESSAGE_GET_WARM_INTERVAL && batchId != EMPTY_BATCH_ID) {
                    logger.info("[cdc-runner] get message from canal server use too much times, use timeMs : {}, batchId : {}"
                            , getMessageWatch.getLastTaskTimeMillis(), message.getId());
                }
            } catch (Exception e) {
                //  未获取到数据,回滚
                cdcConnector.rollback();
                e.printStackTrace();
                String error = String.format("get message from canal server error, %s", e);
                logger.error("[cdc-runner] {}", error);
                throw new SQLException(error);
            }


            //  当synced标志位设置为True时，表示后续的操作必须通过最终一致性操作保持成功
            boolean synced = false;
            try {
                CDCMetrics cdcMetrics = null;
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                    //  消费binlog
                    cdcMetrics = consumerService.consume(message.getEntries(), batchId, cdcMetricsService);

                    //  binlog处理，同步指标到cdcMetrics中
                    synced = backMetrics(cdcMetrics);

                    //  canal状态确认、指标同步
                    syncSuccess(cdcMetrics);
                } else {

                    //  当前没有任务需要消费
                    cdcMetrics = new CDCMetrics(batchId, cdcMetricsService.getCdcMetrics().getCdcAckMetrics(),
                            cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());
                    cdcMetrics.getCdcAckMetrics().setExecuteRows(ZERO);

                    synced = backMetrics(cdcMetrics);

                    syncFree(batchId);
                }
            } catch (Exception e) {
                String error = "";
                if (!synced) {
                    //  当未执行到最终必须成功时,需进行rollback
                    cdcConnector.rollback();
                    error = "consume message error";
                } else {
                    error = "sync finish status error";
                }
                e.printStackTrace();
                logger.error("[cdc-runner] sync error, will reconnect..., message : {}, {}", error, e.getMessage());
                throw new SQLException(error);
            }
        }
    }

    //  首先保存本次消费完时未提交的数据
    private boolean backMetrics(CDCMetrics cdcMetrics) {
        cdcMetricsService.backup(cdcMetrics);
        return true;
    }

    private void syncFree(long batchId) throws SQLException {

        //  同步状态
        cdcConnector.ack(batchId);

//        cdcMetricsService.syncFreeMessage(EMPTY_BATCH_ID);

        //  没有新的同步信息，睡眠1秒进入下次轮训
        threadSleep(FREE_MESSAGE_WAIT_IN_SECONDS);
    }

    private void syncAndRecover() throws SQLException {

        //  设置cdc连接成功
        cdcMetricsService.connectOk();

        //  如果是服务重启，则需要对齐canal ack信息及redis中的ackMetrics指标
        //  查询
        CDCMetrics cdcMetrics = cdcMetricsService.query();
        long originBatchId = EMPTY_BATCH_ID;
        if (null != cdcMetrics) {
            //  当前的BatchId != -1时，表示需要进行Canal batchId ACK操作
            originBatchId = cdcMetrics.getBatchId();
            if (originBatchId != IS_BACK_UP_ID && originBatchId != EMPTY_BATCH_ID) {
                // ack确认， 回写uncommit信息
                backAfterAck(originBatchId, cdcMetrics);
            }

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, true);

            logger.info("[cdc-runner] recover from last ackMetrics position success..., originBatchId : {}", originBatchId);
        } else {
            //  回调告知当前为一个新的开始
            cdcMetricsService.newConnectCallBack();
            logger.info("[cdc-runner] new connect callBack success, originBatchId : {}", originBatchId);
        }

        //  确认完毕，需要将当前未提交的数据回滚到当前已确认batchId所对应的初始位置
        cdcConnector.rollback();
    }

    /*
        关键步骤
     */
    private void syncSuccess(CDCMetrics cdcMetrics) throws SQLException {
        if (null != cdcMetrics) {
            long originBatchId = cdcMetrics.getBatchId();
            // ack确认， 回写unCommit信息
            backAfterAck(originBatchId, cdcMetrics);

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, false);
        }
    }
    /*
        由于采用2阶段prepare -> confirm模式，当进入backAfterAck的逻辑时,必须保证一致性（成功）
        所以需要在ack成功后标记batchId为-Long.MAX_VALUE，并覆盖unCommitMetrics。
        启动时重复该步骤
     */
    private void backAfterAck(long originBatchId, CDCMetrics cdcMetrics) throws SQLException {
        //  1.确认ack batchId
        cdcConnector.ack(originBatchId);
        logger.debug("ack batch success, batchId : {}", originBatchId);
        //  2.设置当前batchId为-LONG.MAX_VALUE
        cdcMetrics.setBatchId(IS_BACK_UP_ID);
        //  3.重置redis unCommit数据
        cdcMetricsService.backup(cdcMetrics);
        logger.debug("rest cdcMetrics with buckUpId success, origin batchId : {}", originBatchId);
    }

    private void threadSleep(int waitInSeconds) {
        try {
            //  当前没有binlog消费
            Thread.sleep(waitInSeconds);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void callBackError(int waitInSeconds, CDCStatus cdcStatus) {
        threadSleep(waitInSeconds);

        cdcMetricsService.callBackError(cdcStatus);
    }

    private void callBackSuccess(long originBatchId, CDCMetrics cdcMetrics, boolean isConnectSync) {
        cdcMetricsService.callBackSuccess(originBatchId, cdcMetrics, isConnectSync);
    }
}
