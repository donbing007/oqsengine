package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCUnCommitMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.*;

/**
 * desc :
 * name : ConsumerThread
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class ConsumerRunner extends Thread {

    final Logger logger = LoggerFactory.getLogger(CDCDaemonService.class);

    private ConsumerService consumerService;

    private CDCMetricsService cdcMetricsService;

    private CDCConnector cdcConnector;

    private RunningStatus runningStatus;

    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsService cdcMetricsService,
                          CDCConnector cdcConnector) {

        this.consumerService = consumerService;
        this.cdcMetricsService = cdcMetricsService;
        this.cdcConnector = cdcConnector;
    }

    public void shutdown() {
        runningStatus = RunningStatus.STOP;
    }

    public void run() {
        runningStatus = RunningStatus.RUN;

        boolean isFirstCycle = true;
        while (true) {
            try {
                connectAndReset(isFirstCycle);
            } catch (Exception e) {
                closeToNextReconnect(!isFirstCycle, String.format("%s, %s", "canal-server connection error, {}", e.getMessage()));
            }
            //  设置标志位，只有在第一次Loop时会从缓存中读取cdcMetrics信息
            isFirstCycle = false;

            try {
                consume();
            } catch (Exception e) {
                closeToNextReconnect(true, String.format("%s, %s", "canal-client consume error, ", e.getMessage()));
            }

            if (checkForStop()) {
                break;
            }
        }
    }

    private void connectAndReset(boolean isFirstTime) throws SQLException {

        cdcConnector.open();

        if (isFirstTime) {
            //  首先将上次记录完整的信息(batchID)确认到Canal中
            syncLastBatch();
        }

        //  由于LastBatch确认后可能存在getPos/ackPos不一致的情况，需要对getPos进行Rollback操作
        cdcConnector.rollback();
    }

    private void closeToNextReconnect(boolean needRollback, String errorMessage) {
        cdcConnector.close(needRollback);
        logger.error(errorMessage);

        //  这里将进行睡眠->同步错误信息->进入下次循环
        callConnectError(RECONNECT_WAIT_IN_SECONDS);
    }

    private boolean checkForStop() {
        if (runningStatus.equals(RunningStatus.STOP)) {
            try {
                cdcConnector.shutdown();
                consumerService.shutdown();
            } catch (Exception e) {
                //  ignore
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public void consume() throws SQLException {
        while (true) {
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
                long batchId = message.getId();
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                    //  binlog处理，同步指标到cdcMetrics中
                    CDCMetrics cdcMetrics =
                            consumerService.consume(message.getEntries(), batchId, cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());

                    //  notice: canal状态确认、指标同步
                    sync(cdcMetrics);
                } else {
                    //  没有新的同步信息，睡眠1秒进入下次轮训
                    threadSleep(FREE_MESSAGE_WAIT_IN_SECONDS);

                    syncFree(batchId);
                }
            } catch (Exception e) {
                cdcConnector.rollback();

                logger.error("consume message error, {}", e.getMessage());
                //  同步出错信息，回滚到上次成功的的Sync信息
                callBackError();
            }

            //  服务被终止
            if (runningStatus.equals(RunningStatus.STOP)) {
                break;
            }
        }
    }

    private void syncFree(long batchId) throws SQLException {
        CDCMetrics cdcMetrics = new CDCMetrics(batchId, cdcMetricsService.getCdcMetrics().getCdcAckMetrics(),
                                    cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());

        cdcMetricsService.backup(cdcMetrics);

        //  同步状态
        cdcConnector.ack(batchId);

        cdcMetricsService.getCdcMetrics().setBatchId(batchId);
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setLastConnectedTime(System.currentTimeMillis());
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());
    }

    private void syncLastBatch() throws SQLException {
        CDCMetrics cdcMetrics = cdcMetricsService.query();
        if (null != cdcMetrics) {
            syncCanalAndCallback(cdcMetrics);
            logger.debug("CDC启动Recover同步/回调成功, {}", JSON.toJSON(cdcMetrics));
        }
    }
    /*
        关键步骤
     */
    private void sync(CDCMetrics cdcMetrics) throws SQLException {
        if (null != cdcMetrics) {
            //  首先保存本次消费完时未提交的数据
            cdcMetricsService.backup(cdcMetrics);

            //  canal ack确认，同步CDC确认信息，
            syncCanalAndCallback(cdcMetrics);
        }
    }

    private void syncCanalAndCallback(CDCMetrics cdcMetrics) throws SQLException {

        //  ack canal-server 当前位点
        if (cdcMetrics.getBatchId() != EMPTY_BATCH_ID) {
            cdcConnector.ack(cdcMetrics.getBatchId());
        }

        //  重置cdcUnCommit信息
        syncUnCommit(cdcMetrics.getCdcUnCommitMetrics());

        //  回调告知当前成功信息
        callBackSuccess(cdcMetrics.getCdcAckMetrics());
    }

    private void syncUnCommit(CDCUnCommitMetrics unCommitMetrics) {
        if (null == unCommitMetrics) {
            unCommitMetrics = new CDCUnCommitMetrics();
        }
        cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(unCommitMetrics);
    }

    private void callConnectError(int waitInSeconds) {
        threadSleep(waitInSeconds);

        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);

        cdcMetricsService.callback();
    }

    private void threadSleep(int waitInSeconds) {
        try {
            //  当前没有Binlog消费
            Thread.sleep(waitInSeconds * 1000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callBackError() {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.CONSUME_FAILED);

        cdcMetricsService.callback();
    }

    private void callBackSuccess(CDCAckMetrics currentMetrics) {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setLastConsumerTime(System.currentTimeMillis());

        if (!currentMetrics.getCommitList().isEmpty()) {
            cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCommitList(currentMetrics.getCommitList());
        }

        if (currentMetrics.getMaxSyncUseTime() > ZERO) {
            cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setMaxSyncUseTime(currentMetrics.getMaxSyncUseTime());
        }

        cdcMetricsService.callback();
    }
}
