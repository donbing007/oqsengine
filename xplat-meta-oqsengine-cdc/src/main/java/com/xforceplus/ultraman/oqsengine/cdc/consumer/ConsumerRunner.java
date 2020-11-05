package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCMetricsConstant.*;

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

    private CDCMetricsCallback cdcMetricsCallback;

    private CDCMetrics cdcMetrics;

    private ExecutorService cdcSyncPool;

    private CDCConnector cdcConnector;

    public ConsumerRunner(ConsumerService consumerService,
                          CDCMetricsCallback cdcMetricsCallback,
                          CDCConnector cdcConnector) {

        this.consumerService = consumerService;
        this.cdcMetricsCallback = cdcMetricsCallback;
        this.cdcConnector = cdcConnector;

        //  启动一个线程数大小为1线程池进行CDC指标的同步
        cdcSyncPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
                ExecutorHelper.buildNameThreadFactory(POOL_NAME, true),
                new ThreadPoolExecutor.AbortPolicy()
        );

        cdcMetrics = new CDCMetrics(CDCStatus.CONNECTED);
    }

    private boolean connectAndReset() {
        boolean isConnected = false;
        try {
            cdcConnector.getCanalConnector().connect();
            isConnected = true;
            //监听的表，    格式为：数据库.表名,数据库.表名
            cdcConnector.getCanalConnector().subscribe(cdcConnector.getSubscribeFilter());
            cdcConnector.getCanalConnector().rollback();

            return true;
        } catch (Exception e) {
            if (isConnected) {
                cdcConnector.getCanalConnector().disconnect();
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
                    cdcConnector.getCanalConnector().disconnect();
                }
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
                message = cdcConnector.getCanalConnector().getWithoutAck(cdcConnector.getBatchSize());
            } catch (Exception e) {
                cdcConnector.getCanalConnector().rollback();

                String error = String.format("get message error, %s", e);
                logger.error(error);
                throw new SQLException(error);
            }

            long lastMaxSyncUseTime = cdcMetrics.getMaxSyncUseTime();

            try {
                long batchId = message.getId();
                if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                    //  binlog处理，同步指标到cdcMetrics中
                    CDCMetrics currentMetrics = consumerService.consume(message.getEntries());

                    //  必须先ACK，才能进行回调
                    cdcConnector.getCanalConnector().ack(batchId);

                    //  回调
                    callBackSuccess(currentMetrics);
                } else {
                    //  没有新的同步信息，睡眠1秒进入下次轮训
                    threadSleep(FREE_MESSAGE_WAIT_IN_SECONDS);

                    //  同步状态
                    cdcConnector.getCanalConnector().ack(batchId);
                }
            } catch (Exception e) {
                cdcConnector.getCanalConnector().rollback();
                logger.error("consume message error, {}", e.getMessage());
                //  同步出错信息，回滚到上次成功的的Sync信息
                callBackError(lastMaxSyncUseTime);
            }
        }
    }


    private void callConnectError(int waitInSeconds) {
        threadSleep(waitInSeconds);

        cdcMetrics.setCdcConsumerStatus(CDCStatus.DIS_CONNECTED);

        callback();
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
        cdcMetrics.setCdcConsumerStatus(CDCStatus.CONSUME_FAILED);
        cdcMetrics.setMaxSyncUseTime(lastMaxSyncUseTime);

        callback();
    }

    private void callBackSuccess(CDCMetrics currentMetrics) {
        cdcMetrics.setLastConsumerTime(System.currentTimeMillis());

        if (!currentMetrics.getCommitList().isEmpty()) {
            cdcMetrics.setCommitList(currentMetrics.getCommitList());
        }

        if (currentMetrics.getMaxSyncUseTime() > ZERO) {
            cdcMetrics.setMaxSyncUseTime(currentMetrics.getMaxSyncUseTime());
        }

        callback();
    }

    private void callback() {
        //  设置本次callback的时间
        cdcMetrics.setLastUpdateTime(System.currentTimeMillis());
        //  异步执行回调
        cdcSyncPool.submit(() -> {
            try {
                cdcMetricsCallback.cdcCallBack(cdcMetrics);
            } catch (Exception e) {
                logger.error("callback error, metrics : {}", cdcMetrics.toString());
                e.printStackTrace();
            }
        });
    }
}
