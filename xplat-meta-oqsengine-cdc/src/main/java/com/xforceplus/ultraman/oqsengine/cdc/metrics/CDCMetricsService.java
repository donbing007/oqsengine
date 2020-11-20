package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.*;


/**
 * desc :
 * name : CDCMetricsService
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCMetricsService {

    final Logger logger = LoggerFactory.getLogger(CDCMetricsService.class);

    @Resource
    private CDCMetricsCallback cdcMetricsCallback;

    //  sync pool
    private ExecutorService cdcSyncPool;

    private CDCMetrics cdcMetrics;

    private volatile boolean shutdown;

    public CDCMetricsService() {
        cdcMetrics = new CDCMetrics();
        initCdcSyncPool();
        shutdown = false;
    }

    private void initCdcSyncPool() {
        //  启动一个线程数大小为1线程池进行CDC指标的同步
        cdcSyncPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
            ExecutorHelper.buildNameThreadFactory(POOL_NAME, true),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void startMetrics() {
        shutdown = false;
        //  设置心跳
        cdcSyncPool.submit(() -> {
            while (true) {
                if (shutdown) {
                    break;
                }
                try {
                    Thread.sleep(HEART_BREAK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cdcMetricsCallback.heartBeat();
            }
        });
    }

    public void shutdown() {
        shutdown = true;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    public void callBackSuccess(CDCMetrics temp, boolean isConnectSync) {
        //  logger.debug("success consumer, cdcStatus : {}", JSON.toJSON(temp));
        cdcMetrics.setCdcUnCommitMetrics(temp.getCdcUnCommitMetrics());
        cdcMetrics.consumeSuccess(temp, isConnectSync);
        callback();
    }

    public void callBackError(CDCStatus cdcStatus) {
//        logger.debug("error, cdcStatus : {}", cdcStatus);
        cdcMetrics.getCdcAckMetrics().setCdcConsumerStatus(cdcStatus);
        callback();
    }

    public void syncFreeMessage(long batchId) {
        cdcMetrics.syncFreeMessage(batchId);
        callback();
    }

    public void backup(CDCMetrics cdcMetrics) {
        cdcSyncPool.submit(() -> {
            try {
                cdcMetricsCallback.cdcSaveLastUnCommit(cdcMetrics);
            } catch (Exception e) {
                logger.error("back up unCommitMetrics to redis error, unCommitMetrics : {}", JSON.toJSON(cdcMetrics));
                e.printStackTrace();
            }
        });
    }

    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("query unCommitMetrics from redis error.");
        }
    }

    public void connectedOk() {
        cdcMetrics.resetStatus();
    }

    private void callback() {
        try {
            logger.debug("callback ack metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
        } catch (Exception ex) {
            logger.debug("print ack metrics error.");
            ex.printStackTrace();
        }

        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());
        //  异步执行回调
        cdcSyncPool.submit(() -> {
            try {
                cdcMetricsCallback.cdcAck(cdcMetrics.getCdcAckMetrics());
            } catch (Exception e) {
                try {
                    logger.error("callback error, metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));

                    e.printStackTrace();
                } catch (Exception ee) {
                    //  ignore
                }
            }
        });

    }


}
