package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_LOG_INTERVAL;


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

    private CDCMetrics cdcMetrics;

    private volatile boolean shutdown;

    public CDCMetricsService() {
        cdcMetrics = new CDCMetrics();
        shutdown = false;
    }
    public void startMetrics() {
        logger.info("[cdc-metrics] start, it will start hearBeat thread");
        Thread heartBeat = new Thread(this::heartBeat);
        heartBeat.setName("cdc-heartBeat");
        heartBeat.start();
        logger.info("[cdc-metrics] hearBeat thread start ok...");
    }

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

    public void shutdown() {
        shutdown = true;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    public void callBackSuccess(long originBatchId, CDCMetrics temp, boolean isConnectSync) {
        //  logger.debug("success consumer, cdcStatus : {}", JSON.toJSON(temp));
        cdcMetrics.setCdcUnCommitMetrics(temp.getCdcUnCommitMetrics());
        cdcMetrics.consumeSuccess(originBatchId, temp, isConnectSync);
        callback();
    }

    public void callBackError(CDCStatus cdcStatus) {
//        logger.debug("error, cdcStatus : {}", cdcStatus);
        cdcMetrics.getCdcAckMetrics().setCdcConsumerStatus(cdcStatus);
        callback();
    }

    public void syncFreeMessage(long batchId) {
        cdcMetrics.syncFreeMessage(batchId);
        //callback();
    }

    public void backup(CDCMetrics cdcMetrics) {
        try {
            cdcMetricsCallback.cdcSaveLastUnCommit(cdcMetrics);
        } catch (Exception e) {
            logger.error("[cdc-metrics] back up unCommitMetrics to redis error, unCommitMetrics : {}", JSON.toJSON(cdcMetrics));
        }
    }

    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            throw new SQLException("[cdc-metrics] query unCommitMetrics from redis error.");
        }
    }

    public void isReadyCommit(long commitId) {
        StopWatch timer = new StopWatch();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-metrics] attempt check ready to commitId , commitId : {}", commitId);
            }
            timer.start();
            int loops = 0;
            while (true) {
                if (!cdcMetricsCallback.isReadyCommit(commitId)) {
                    try {
                        Thread.sleep(COMMIT_ID_READY_CHECK_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    loops++;
                    if (loops > COMMIT_ID_LOG_MAX_LOOPS) {
                        int totalLoops = 0;
                        totalLoops += loops;
                        loops = 0;
                        logger.warn(
                            "[cdc-metrics] loops for wait ready commit missed current check point, current-wait-time : {}ms, commitId : {}"
                            , totalLoops * COMMIT_ID_READY_CHECK_INTERVAL, commitId);
                    }
                    continue;
                }
                break;
            }
        } finally {
            timer.stop();
            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-metrics] success check ready to commitId, commitId : {}", commitId);
            }
            if (timer.getLastTaskTimeMillis() > READY_WARM_MAX_INTERVAL) {
                logger.warn("[cdc-metrics] wait for ready commitId use too much times, commitId {}, use time : {}ms"
                        , commitId, timer.getLastTaskTimeMillis());
            }
        }

    }

    public void connectOk() {
        cdcMetrics.resetStatus();
    }

    public void newConnectCallBack() {
        callback();
    }

    private void callback() {

        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());

        try {
            logger.debug("[cdc-metrics] callback ack metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
        } catch (Exception ex) {
            logger.debug("[cdc-metrics] print ack metrics error, message : {}", ex.getMessage());
        }

        //  执行回调
        try {
            cdcMetricsCallback.cdcAck(cdcMetrics.getCdcAckMetrics());
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
