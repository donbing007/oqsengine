package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.COMMIT_ID_LOG_MAX_LOOPS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.COMMIT_ID_READY_CHECK_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.READY_WARM_MAX_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_LOG_INTERVAL;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * desc :.
 * name : CDCMetricsService
 *
 * @author : xujia 2020/11/7
 * @since : 1.8
 */
public class DefaultCDCMetricsHandler implements CDCMetricsHandler {

    final Logger logger = LoggerFactory.getLogger(DefaultCDCMetricsHandler.class);

    @Resource
    private CDCMetricsCallback cdcMetricsCallback;

    private volatile boolean shutdown;

    public DefaultCDCMetricsHandler() {
        shutdown = false;
    }

    /**
     * 开始记录.
     */
    @Override
    public void init() {
        logger.info("[cdc-metrics] start, it will start hearBeat thread");
        Thread heartBeat = new Thread(this::heartBeat);
        heartBeat.setName("cdc-heartBeat");
        heartBeat.start();
        logger.info("[cdc-metrics] hearBeat thread start ok...");
    }

    /**
     * 心跳.
     */
    @Override
    public void heartBeat() {
        shutdown = false;
        long lastHeartBeatTime = 0;

        //  设置心跳
        while (true) {
            if (shutdown) {
                break;
            }

            try {
                cdcMetricsCallback.heartBeat();
                long now = System.currentTimeMillis();
                if (now - lastHeartBeatTime > HEART_BEAT_LOG_INTERVAL) {
                    lastHeartBeatTime = now;
                    logger.debug("[cdc-metrics] current heartBeat timeStamps : {}", lastHeartBeatTime);
                }
                Thread.sleep(HEART_BEAT_INTERVAL);
            } catch (Exception e) {
                logger.warn("[cdc-metrics] heartBeat error, message :{}", e.getMessage());
            }
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void callBackSuccess(CDCMetrics cdcMetrics) {

        callback(cdcMetrics);

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-metrics] success consumer, cdcMetrics : {}, batchId : {}", JSON.toJSON(cdcMetrics),
                cdcMetrics.getBatchId());
        }
    }

    @Override
    public void callBackError(CDCMetrics cdcMetrics) {
        logger.warn("[cdc-metrics] callback error, cdcStatus : {}",
            cdcMetrics.getCdcAckMetrics().getCdcConsumerStatus());
        callback(cdcMetrics);
    }

    @Override
    public void backup(CDCMetrics cdcMetrics) {
        try {
            cdcMetricsCallback.saveLastUnCommit(cdcMetrics);
        } catch (Exception e) {
            logger.error("[cdc-metrics] back up unCommitMetrics to redis error, batch : {}, unCommitMetrics : {}",
                cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics));
        }
    }

    @Override
    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            throw new SQLException("[cdc-metrics] query unCommitMetrics from redis error.");
        }
    }

    @Override
    public void isReady(List<Long> commitIds) {
        long start = System.currentTimeMillis();
        int loops = 0;
        boolean recoverMonitor = false;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-metrics] attempt check ready to commitIds , commitIds : {}", commitIds);
            }
            while (true) {
                //  传入commitId列表、返回列表中notReady的部分.
                commitIds = cdcMetricsCallback.notReady(commitIds);
                if (commitIds.isEmpty()) {
                    break;
                }

                try {
                    Thread.sleep(COMMIT_ID_READY_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    //  ignore
                }

                loops++;

                if (loops > COMMIT_ID_LOG_MAX_LOOPS) {
                    recoverMonitor = true;
                    loops = 0;
                    logger.warn(
                        "[cdc-metrics] loops for wait ready commit missed current check point (10s), commitId : {}",
                        commitIds);

                    //  输出NotReady指标
                    commitIds.forEach(this::notReady);
                }
            }
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-metrics] success check ready to commitIds, commitIds : {}", commitIds);
            }

            if (duration > READY_WARM_MAX_INTERVAL) {
                logger.warn("[cdc-metrics] wait for ready commitId use too much times, commitIds {}, use time : {}ms",
                    commitIds, duration);
            }

            if (recoverMonitor) {
                //  恢复isReady指标
                notReady(INIT_ID);
            }
        }

    }

    @Override
    public void renewConnect(CDCMetrics cdcMetrics) {
        callback(cdcMetrics);
    }

    private void notReady(long commitId) {
        cdcMetricsCallback.notReady(commitId);
    }

    private void callback(CDCMetrics cdcMetrics) {

        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());

        try {
            logger.debug("[cdc-metrics] callback ack metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
        } catch (Exception ex) {
            logger.debug("[cdc-metrics] print ack metrics error, message : {}", ex.getMessage());
        }

        //  执行回调
        try {
            cdcMetricsCallback.ack(cdcMetrics.getCdcAckMetrics());
        } catch (Exception e) {
            try {
                logger.error("[cdc-metrics] callback error, metrics : {}, message : {}",
                    JSON.toJSON(cdcMetrics.getCdcAckMetrics()), e.getMessage());
            } catch (Exception ee) {
                //  ignore
            }
        }
    }
}
