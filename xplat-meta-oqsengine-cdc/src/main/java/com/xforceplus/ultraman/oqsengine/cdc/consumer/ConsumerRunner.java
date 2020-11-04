package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.config.CDCConsumerProperties;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

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

    private CanalConnector canalConnector;

    private ConsumerService consumerService;
    private CDCConsumerProperties cdcConsumerProperties;

    private CDCMetricsCallback cdcMetricsCallback;

    private CDCMetrics cdcMetrics;

    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsCallback cdcMetricsCallback,
                          CDCConsumerProperties cdcConsumerProperties) {

        this.consumerService = consumerService;
        this.cdcConsumerProperties = cdcConsumerProperties;
        this.cdcMetricsCallback = cdcMetricsCallback;

        canalConnector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(cdcConsumerProperties.getCdcConnectString(),
                                        cdcConsumerProperties.getCdcConnectPort()), cdcConsumerProperties.getCdcDestination(),
                                        cdcConsumerProperties.getCdcUserName(), cdcConsumerProperties.getCdcPassword());

        cdcMetrics = new CDCMetrics(CDCStatus.CONNECTED);
    }

    private boolean connectAndReset() {
        boolean isConnected = false;
        try {
            canalConnector.connect();
            isConnected = true;
            //监听的表，    格式为：数据库.表名,数据库.表名
            canalConnector.subscribe(cdcConsumerProperties.getSubscribeFilter());
            canalConnector.rollback();

            return true;
        } catch (Exception e) {
            if (isConnected) {
                canalConnector.disconnect();
                logger.error("consumer/canal-server connection prepare error.");
            } else {
                logger.error("consumer/canal-server connection error.");
            }
        }
        return false;
    }

    public void run() {

        while (true) {
            if (connectAndReset()) {
                try {
                    consume();
                } catch (Exception e) {
                    canalConnector.disconnect();
                }
            }

            //  wait for reconnected
            callConnectError(cdcConsumerProperties.getReconnectWaitInSeconds());
        }
    }

    public void consume() {
        while (true) {
            Message message = null;
            try {
                //获取指定数量的数据
                message = canalConnector.getWithoutAck(cdcConsumerProperties.getBatchSize());
            } catch (Exception e) {
                logger.error("get message error, {}", e.getMessage());
                canalConnector.rollback();
                canalConnector.disconnect();
                break;
            }

            long lastMaxSyncUseTime = cdcMetrics.getMaxSyncUseTime();

            try {
                long batchId = message.getId();
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                    //  这里会设置最大消费间隔
                    consumerService.consume(message.getEntries(), cdcMetrics);

                    //  回写ACK
                    canalConnector.ack(batchId);

                    //  回调
                    callBackSuccess();
                } else {
                    try {
                        //  当前没有Binlog消费
                        Thread.sleep(DEFAULT_FREE_MESSAGE_WAIT_IN_SECONDS * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    canalConnector.ack(batchId);
                }
            } catch (Exception e) {
                logger.error("consume message error, {}", e.getMessage());
                canalConnector.rollback();

                callBackError(lastMaxSyncUseTime);
            }
        }
    }

    private void callConnectError(int waitInSeconds) {
        try {
            //  当前没有Binlog消费
            Thread.sleep(waitInSeconds * 1000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cdcMetrics.setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);

        callback();
    }

    private void callBackError(long lastMaxSyncUseTime) {
        cdcMetrics.setCdcConsumerStatus(CDCStatus.CONSUME_FAILED);
        cdcMetrics.setMaxSyncUseTime(lastMaxSyncUseTime);

        callback();
    }

    private void callBackSuccess() {
        cdcMetrics.setLastConsumerTime(System.currentTimeMillis());
        callback();
    }

    private void callback() {
        cdcMetrics.setLastUpdateTime(System.currentTimeMillis());
        try {
            cdcMetricsCallback.cdcCallBack(cdcMetrics);
        } catch (Exception e) {
            logger.error("callback error, metrics : {}", cdcMetrics.toString());
            e.printStackTrace();
        }
    }
}
