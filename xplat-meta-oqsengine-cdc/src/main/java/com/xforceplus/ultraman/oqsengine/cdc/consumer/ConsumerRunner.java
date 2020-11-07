package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
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

    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsService cdcMetricsService,
                          CDCConnector cdcConnector) {

        this.consumerService = consumerService;
        this.cdcMetricsService = cdcMetricsService;
        this.cdcConnector = cdcConnector;
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

    public void run() {
        boolean isFirstCycle = true;
        while (true) {
            try {
                connectAndReset(isFirstCycle);
            } catch (Exception e) {
                cdcConnector.close(!isFirstCycle);
                logger.error("canal-server/canal-client connection error, cause : {}", e.getMessage());
            }

            isFirstCycle = false;
            try {
                consume();
            } catch (Exception e) {
                cdcConnector.close(true);
            }

            //  这里将进行睡眠->同步错误信息->进入下次循环
            callConnectError(RECONNECT_WAIT_IN_SECONDS);
        }
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

            //  缓存上一次最大消耗时间
            long lastMaxSyncUseTime = cdcMetricsService.getCdcMetrics().getCdcAckMetrics().getMaxSyncUseTime();
            try {
                long batchId = message.getId();
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {
                    //  binlog处理，同步指标到cdcMetrics中
                    CDCMetrics cdcMetrics = consumerService.consume(message.getEntries(),
                                            cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics());

                    //  notice: canal状态确认、指标同步
                    sync(cdcMetrics, batchId);
                } else {
                    //  没有新的同步信息，睡眠1秒进入下次轮训
                    threadSleep(FREE_MESSAGE_WAIT_IN_SECONDS);

                    //  同步状态
                    cdcConnector.ack(batchId);
                }
            } catch (Exception e) {
                cdcConnector.rollback();

                logger.error("consume message error, {}", e.getMessage());
                //  同步出错信息，回滚到上次成功的的Sync信息
                callBackError(lastMaxSyncUseTime);
            }
        }
    }

    private void syncLastBatch() throws SQLException {
        CDCMetrics cdcMetrics = cdcMetricsService.query();

        syncCanalAndCallback(cdcMetrics);
    }
    /*
        关键步骤
     */
    private void sync(CDCMetrics cdcMetrics, long batchId) throws SQLException {

        cdcMetrics.setBatchId(batchId);

        //  首先保存本次消费完时未提交的数据
        cdcMetricsService.backup(cdcMetrics);

        //  canal ack确认，同步CDC确认信息，
        syncCanalAndCallback(cdcMetrics);
    }

    private void syncCanalAndCallback(CDCMetrics cdcMetrics) throws SQLException {

        //  ack canal-server 当前位点
        cdcConnector.ack(cdcMetrics.getBatchId());

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

    private void callBackError(long lastMaxSyncUseTime) {
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(CDCStatus.CONSUME_FAILED);
        cdcMetricsService.getCdcMetrics().getCdcAckMetrics().setMaxSyncUseTime(lastMaxSyncUseTime);

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
