package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_ID;
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

    private CDCMetrics cdcMetrics;

    private volatile boolean shutdown;

    public CDCMetricsService() {
        cdcMetrics = new CDCMetrics();
        shutdown = false;
    }
    public void startMetrics() {
        logger.info("cdc metrics start, it will start hearBeat thread");
        Thread heartBeat = new Thread(this::heartBeat);
        heartBeat.setName("cdc-heartBeat");
        heartBeat.start();
        logger.info("cdc metrics hearBeat thread start ok...");
    }

    public void heartBeat() {
        shutdown = false;
        //  设置心跳
        while (true) {
            if (shutdown) {
                break;
            }

            try {
                cdcMetricsCallback.heartBeat();
                logger.debug("cdc current heartBeat timeStamps : {}", System.currentTimeMillis());
                Thread.sleep(HEART_BEAT_INTERVAL);
            } catch (Exception e) {
                logger.warn("cdc heartBeat error, message :{}", e.getMessage());
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
        if (originBatchId != EMPTY_BATCH_ID) {
            callback();
        }
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
            logger.error("back up unCommitMetrics to redis error, unCommitMetrics : {}", JSON.toJSON(cdcMetrics));
        }
    }

    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            throw new SQLException("query unCommitMetrics from redis error.");
        }
    }

    public void connectedOk() {
        cdcMetrics.resetStatus();
    }

    private void callback() {

        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());

        try {
            logger.debug("callback ack metrics : {}", JSON.toJSON(cdcMetrics.getCdcAckMetrics()));
        } catch (Exception ex) {
            logger.debug("print ack metrics error, message : {}", ex.getMessage());
        }

        //  执行回调
        try {
            cdcMetricsCallback.cdcAck(cdcMetrics.getCdcAckMetrics());
        } catch (Exception e) {
            try {
                logger.error("callback error, metrics : {}, message : {}",
                                JSON.toJSON(cdcMetrics.getCdcAckMetrics()), e.getMessage());
            } catch (Exception ee) {
                //  ignore
            }
        }
    }
}
