package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        int useTime = 0;
        while (useTime < MAX_STOP_WAIT_LOOPS) {
            try {
                Thread.sleep(MAX_STOP_WAIT_TIME * SECOND);
            } catch (Exception e) {
                //  ignore
                e.printStackTrace();
            }
            if (isShutdown()) {
                logger.info("cdc consumer success stop.");
                break;
            }

            useTime++;
        }
        if (useTime >= MAX_STOP_WAIT_LOOPS) {
            logger.warn("cdc consumer force stop after {} seconds.", useTime * MAX_STOP_WAIT_TIME);
        }
    }

    public boolean isShutdown() {
        return runningStatus.equals(RunningStatus.STOP_SUCCESS);
    }

    public void run() {
        runningStatus = RunningStatus.INIT;
        while (true) {
            //  判断当前服务状态是否可运行
            if (checkForStop()) {
                break;
            }

            try {
                //  连接CanalServer，如果是服务启动(runningStatus = INIT),则同步缓存中cdcMetrics信息
                connectAndReset();
            } catch (Exception e) {
                closeToNextReconnect(CDCStatus.DIS_CONNECTED, !runningStatus.equals(RunningStatus.INIT)
                    , String.format("%s, %s", "canal-server connection error", e.getMessage()));
                continue;
            }
            //  连接成功，重置标志位
            runningStatus = RunningStatus.RUN;

            try {
                //  开始消费
                consume();
            } catch (Exception e) {
                closeToNextReconnect(CDCStatus.CONSUME_FAILED,
                        true, String.format("%s, %s", "canal-client consume error", e.getMessage()));
            }
        }
    }

    private void connectAndReset() throws SQLException {

        cdcConnector.open();

        //  首先将上次记录完整的信息(batchID)确认到Canal中
        syncAndRecover();

        //  由于LastBatch确认后可能存在getPos/ackPos不一致的情况，需要对getPos进行Rollback操作
        cdcConnector.rollback();
    }

    private void closeToNextReconnect(CDCStatus cdcStatus, boolean needRollback, String errorMessage) {
        cdcConnector.close(needRollback);
        logger.error(errorMessage);

        //  这里将进行睡眠->同步错误信息->进入下次循环
        callBackError(RECONNECT_WAIT_IN_SECONDS, cdcStatus);
    }

    private boolean checkForStop() {
        if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
            try {
                cdcConnector.shutdown();
            } catch (Exception e) {
                //  ignore
                e.printStackTrace();
            }
            runningStatus = RunningStatus.STOP_SUCCESS;
            return true;
        }
        return false;
    }

    public void consume() throws SQLException {
        while (true) {
            //  服务被终止
            if (runningStatus.ordinal() >= RunningStatus.TRY_STOP.ordinal()) {
                runningStatus = RunningStatus.STOP_SUCCESS;
                break;
            }

            Message message = null;
            try {
                //获取指定数量的数据
                message = cdcConnector.getMessageWithoutAck();
            } catch (Exception e) {
                String error = String.format("get message error, %s", e);
                logger.error(error);
                throw new SQLException(error);
            }

            try {
                CDCMetrics cdcMetrics = null;
                long batchId = message.getId();
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                    //  binlog处理，同步指标到cdcMetrics中
                    cdcMetrics =
                        consumerService.consume(message.getEntries(), batchId, cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());

                    //  notice: canal状态确认、指标同步
                    syncSuccess(cdcMetrics);
                } else {
                    //  没有新的同步信息，睡眠1秒进入下次轮训
                    threadSleep(FREE_MESSAGE_WAIT_IN_SECONDS);

                    syncFree(batchId);
                }
            } catch (Exception e) {
                cdcConnector.rollback();
                String error = String.format("consume message error");
                e.printStackTrace();
                throw new SQLException(error);
            }
        }
    }

    private void syncFree(long batchId) throws SQLException {
        CDCMetrics cdcMetrics = new CDCMetrics(batchId, cdcMetricsService.getCdcMetrics().getCdcAckMetrics(),
            cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());

        cdcMetricsService.backup(cdcMetrics);

        //  同步状态
        cdcConnector.ack(batchId);

        cdcMetricsService.heartBeat(batchId);
    }

    private void syncAndRecover() throws SQLException {

        //  设置cdc连接成功
        cdcMetricsService.connected();

        CDCMetrics cdcMetrics = cdcMetricsService.query();

        if (null != cdcMetrics) {
            syncAndCallback(cdcMetrics, true);
            logger.info("cdc recover from last ackMetrics position success...");
        }
    }

    /*
        关键步骤
     */
    private void syncSuccess(CDCMetrics cdcMetrics) throws SQLException {
        if (null != cdcMetrics) {
            //  首先保存本次消费完时未提交的数据
            cdcMetricsService.backup(cdcMetrics);

            syncAndCallback(cdcMetrics, false);
        }
    }

    private void syncAndCallback(CDCMetrics cdcMetrics, boolean isConnectSync) throws SQLException {
        //  ack canal-server 当前位点
        if (cdcMetrics.getBatchId() != EMPTY_BATCH_ID) {
            cdcConnector.ack(cdcMetrics.getBatchId());
        }

        //  回调告知当前成功信息
        callBackSuccess(cdcMetrics, isConnectSync);
    }

    private void threadSleep(int waitInSeconds) {
        try {
            //  当前没有Binlog消费
            Thread.sleep(waitInSeconds * SECOND);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callBackError(int waitInSeconds, CDCStatus cdcStatus) {
        threadSleep(waitInSeconds);

        cdcMetricsService.callBackError(cdcStatus);
    }

    private void callBackSuccess(CDCMetrics cdcMetrics, boolean isConnectSync) {

        cdcMetricsService.callBackSuccess(cdcMetrics, isConnectSync);
    }
}
