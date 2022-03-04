package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.FREE_MESSAGE_WAIT_IN_MS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.IS_BACK_UP_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_LOOPS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_TIME;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MESSAGE_GET_WARM_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.RECONNECT_WAIT_IN_SECONDS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : ConsumerThread
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public class ConsumerRunner extends Thread {

    final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);

    private ConsumerService consumerService;

    private CDCMetricsService cdcMetricsService;

    private AbstractCDCConnector connector;

    private RebuildIndexExecutor rebuildIndexExecutor;

    private static volatile RunningStatus runningStatus;

    /**
     * 实例化.
     */
    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsService cdcMetricsService,
                          AbstractCDCConnector connector,
                          RebuildIndexExecutor rebuildIndexExecutor) {

        this.consumerService = consumerService;
        this.cdcMetricsService = cdcMetricsService;
        this.connector = connector;
        this.rebuildIndexExecutor = rebuildIndexExecutor;
    }

    /**
     * 优雅停止.
     */
    public void shutdown() {
        runningStatus = RunningStatus.TRY_STOP;
        cdcMetricsService.shutdown();

        int useTime = 0;
        while (useTime < MAX_STOP_WAIT_LOOPS) {
            TimeWaitUtils.wakeupAfter(MAX_STOP_WAIT_TIME, TimeUnit.SECONDS);

            if (runningStatus.equals(RunningStatus.STOP_SUCCESS)) {
                logger.info("[cdc-runner] consumer success stop.");
                break;
            }

            useTime++;
        }

        if (useTime >= MAX_STOP_WAIT_LOOPS) {
            logger.warn("[cdc-runner] force stop after {} seconds.", useTime * MAX_STOP_WAIT_TIME);
        }
    }

    /**
     * 开始运行.
     */
    public void run() {
        runningStatus = RunningStatus.INIT;

        cdcMetricsService.startMetrics();

        int currentConnectTimes = 0;

        while (true) {
            //  判断当前服务状态是否终止
            if (needTerminate()) {
                break;
            }

            try {
                //  连接CanalServer,如果是服务启动(runningStatus = INIT),则同步缓存中cdcMetrics信息
                connectAndReset(currentConnectTimes);
            } catch (Exception e) {
                currentConnectTimes++;
                closeToNextReconnect(CDCStatus.DIS_CONNECTED,
                    String.format("[cdc-runner] canal-server connection error, %s", e.getMessage()));
                continue;
            }

            //  连接成功，重置标志位
            runningStatus = RunningStatus.RUN;
            currentConnectTimes = 0;

            try {
                //  开始消费
                consume();
            } catch (Exception e) {
                closeToNextReconnect(CDCStatus.CONSUME_FAILED,
                    String.format("[cdc-runner] canal-client consume error, %s", e.getMessage()));
            }
        }
    }

    private void connectAndReset(int currentConnectTimes) throws SQLException {
        if (connector.canUseConnector(currentConnectTimes)) {
            //  打开链接
            connector.open();

            //  首先将上次记录完整的信息(batchID)确认到Canal中
            syncAndRecover();
        } else {
            //  这个步骤是当出现了10次重连都失败的情况，会放弃当前的链接，重新创建一个新的链接。
            //  该做法是为了解决当canal-server重启后当前链接失效的情况下还在继续链接.

            //  再次确认当前链接已释放
            connector.close();

            //  重新创建链接
            connector.init();

            //  链接已重置，重新connectAndReset
            connectAndReset(0);
        }
    }

    private void closeToNextReconnect(CDCStatus cdcStatus, String errorMessage) {
        connector.close();
        logger.error(errorMessage);

        //  这里将进行睡眠->同步错误信息->进入下次循环
        callBackError(RECONNECT_WAIT_IN_SECONDS, cdcStatus);
    }

    private boolean needTerminate() {
        if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
            connector.shutdown();
            runningStatus = RunningStatus.STOP_SUCCESS;
            return true;
        }
        return false;
    }

    /**
     * 消费.
     */
    public void consume() throws SQLException {
        while (true) {

            //  服务被终止
            if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
                runningStatus = RunningStatus.STOP_SUCCESS;
                break;
            }

            Message message = null;
            long batchId;
            try {
                long start = System.currentTimeMillis();

                //获取指定数量的数据
                message = connector.getMessageWithoutAck();
                long duration = System.currentTimeMillis() - start;

                batchId = message.getId();

                if (duration > MESSAGE_GET_WARM_INTERVAL && batchId != EMPTY_BATCH_ID) {
                    logger.info(
                        "[cdc-runner] get message from canal server use too much times, use timeMs : {}, batchId : {}",
                        duration, message.getId());
                }
            } catch (Exception e) {
                //  未获取到数据,回滚
                connector.rollback();
                String error = String.format("get message from canal server error, %s", e.toString());
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
                    synced = saveMetrics(cdcMetrics);
                    //  canal状态确认、指标同步
                    finishAck(cdcMetrics);

                    //  同步维护指标.
                    if (!cdcMetrics.getDevOpsMetrics().isEmpty()) {
                        rebuildIndexExecutor.sync(cdcMetrics.getDevOpsMetrics());
                    }
                } else {
                    //  当前没有任务需要消费
                    cdcMetrics = new CDCMetrics(batchId, cdcMetricsService.getCdcMetrics().getCdcAckMetrics(),
                        cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());
                    cdcMetrics.getCdcAckMetrics().setExecuteRows(ZERO);

                    synced = saveMetrics(cdcMetrics);

                    emptyAck(batchId);
                }
            } catch (Exception e) {
                String error = "";
                if (!synced) {
                    //  当未执行到最终必须成功时,需进行rollback
                    connector.rollback();
                    error = "consume message error";
                } else {
                    error = "sync finish status error";
                }
                logger.error("[cdc-runner] sync error, will reconnect..., message : {}, {}", error, e.toString());
                throw new SQLException(error);
            }
        }
    }

    //  首先保存本次消费完时未提交的数据
    private boolean saveMetrics(CDCMetrics cdcMetrics) {
        cdcMetricsService.backup(cdcMetrics);

        return true;
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

            logger.info("[cdc-runner] recover from last ackMetrics position success..., originBatchId : {}",
                originBatchId);
        } else {
            //  回调告知当前为一个新的开始
            cdcMetricsService.newConnectCallBack();
            logger.info("[cdc-runner] new connect callBack success, originBatchId : {}", originBatchId);
        }

        //  确认完毕，需要将当前未提交的数据回滚到当前已确认batchId所对应的初始位置
        connector.rollback();
    }

    //  同步指标数据
    private void finishAck(CDCMetrics cdcMetrics) throws SQLException {
        if (null != cdcMetrics) {
            long originBatchId = cdcMetrics.getBatchId();
            // ack确认， 回写unCommit信息
            backAfterAck(originBatchId, cdcMetrics);

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, false);
        }
    }

    //  同步一个空的batchId
    private void emptyAck(long batchId) throws SQLException {
        //  同步状态
        connector.ack(batchId);

        //  没有新的同步信息，睡眠1秒进入下次轮训
        TimeWaitUtils.wakeupAfter(FREE_MESSAGE_WAIT_IN_MS, TimeUnit.MILLISECONDS);
    }

    /*
        由于采用2阶段prepare -> confirm模式，当进入backAfterAck的逻辑时,必须保证一致性（成功）
        所以需要在ack成功后标记batchId为-Long.MAX_VALUE，并覆盖unCommitMetrics。
        启动时重复该步骤
     */
    private void backAfterAck(long originBatchId, CDCMetrics cdcMetrics) throws SQLException {
        //  1.确认ack batchId
        connector.ack(originBatchId);
        logger.debug("ack batch success, batchId : {}", originBatchId);
        //  2.设置当前batchId为-LONG.MAX_VALUE
        cdcMetrics.setBatchId(IS_BACK_UP_ID);
        //  3.重置redis unCommit数据
        cdcMetricsService.backup(cdcMetrics);
        logger.debug("rest cdcMetrics with buckUpId success, origin batchId : {}", originBatchId);
    }

    private void callBackError(int waitInSeconds, CDCStatus cdcStatus) {
        TimeWaitUtils.wakeupAfter(waitInSeconds, TimeUnit.SECONDS);

        cdcMetricsService.callBackError(cdcStatus);
    }

    private void callBackSuccess(long originBatchId, CDCMetrics cdcMetrics, boolean isConnectSync) {
        cdcMetricsService.callBackSuccess(originBatchId, cdcMetrics, isConnectSync);
    }
}
