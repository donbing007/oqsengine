package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.COMMIT_ID_LOG_MAX_LOOPS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.COMMIT_ID_READY_CHECK_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.READY_WARM_MAX_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.HEART_BEAT_LOG_INTERVAL;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
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

    /**
     * 开始记录.
     */
    public void startMetrics() {
        logger.info("[cdc-metrics] start, it will start hearBeat thread");
        Thread heartBeat = new Thread(this::heartBeat);
        heartBeat.setName("cdc-heartBeat");
        heartBeat.start();
        logger.info("[cdc-metrics] hearBeat thread start ok...");
    }

    /**
     * 心跳.
     */
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

    /**
     * 回调通知成功.
     */
    public void callBackSuccess(long originBatchId, CDCMetrics temp, boolean isConnectSync) {

        cdcMetrics.setCdcUnCommitMetrics(temp.getCdcUnCommitMetrics());
        cdcMetrics.consumeSuccess(originBatchId, temp, isConnectSync);
        callback();

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-metrics] success consumer, cdcMetrics : {}, batchId : {}", JSON.toJSON(temp),
                originBatchId);
        }
    }

    /**
     * 回调通知发生错误.
     */
    public void callBackError(CDCStatus cdcStatus) {
        logger.warn("error, cdcStatus : {}", cdcStatus);
        cdcMetrics.getCdcAckMetrics().setCdcConsumerStatus(cdcStatus);
        callback();
    }

    /**
     * 备份.
     */
    public void backup(CDCMetrics cdcMetrics) {
        try {
            cdcMetricsCallback.saveLastUnCommit(cdcMetrics);
        } catch (Exception e) {
            logger.error("[cdc-metrics] back up unCommitMetrics to redis error, batch : {}, unCommitMetrics : {}",
                cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics));
        }
    }

    /**
     * 查询.
     */
    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            throw new SQLException("[cdc-metrics] query unCommitMetrics from redis error.");
        }
    }

    /**
     * 判断提交号是否已经准备完成.
     * 没有准备将进行等待.
     *
     * @param commitIds 提交号.
     */
    public void isReadyCommit(List<Long> commitIds) {
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

    public void connectOk() {
        cdcMetrics.resetStatus();
    }

    public void newConnectCallBack() {
        callback();
    }

    private void notReady(long commitId) {
        cdcMetricsCallback.notReady(commitId);
    }

    private void callback() {

        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());

        try {
            logger.info("[cdc-metrics] callback ack metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
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
