package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DEFAULT_BATCH_SIZE;

/**
 * desc :
 * name : CDCConnector
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public abstract class CDCConnector {

    final Logger logger = LoggerFactory.getLogger(CDCConnector.class);

    private int batchSize = DEFAULT_BATCH_SIZE;

    protected CanalConnector canalConnector;


    public void shutdown() {
        try {
            if (canalConnector.checkValid()) {
                close();
            }
        } catch (Exception e) {
            logger.warn("[cdc-connector] shutdown error, message : {}", e.getMessage());
        }
    }

    /**
     * 打开canal连接
     */
    public void open() {
        if (null != canalConnector) {
            //  连接CanalServer
            canalConnector.connect();
            //  订阅destination
            canalConnector.subscribe();
            logger.info("[cdc-connector] connect to canal server...");
        }
    }

    /**
     * 关闭canal连接
     */
    public void close() {
        if (null != canalConnector) {
            try {
                logger.error("[cdc-connector] close canal connector...");
                //  关闭连接CanalServer
                canalConnector.disconnect();
            } catch (Exception e) {
                logger.error("[cdc-connector] close error, ex : {}", e.getMessage());
            }
        }
    }

    public void rollback() {
        if (null != canalConnector) {
            canalConnector.rollback();
        }
    }

    public void ack(long batchId) throws SQLException {
        if (null == canalConnector) {
            notInitException();
        }
        canalConnector.ack(batchId);
    }

    public Message getMessageWithoutAck() throws SQLException {
        if (null == canalConnector) {
            notInitException();
        }
        //  获取2048条数据
        return canalConnector.getWithoutAck(batchSize);
    }

    private void notInitException() throws SQLException {
        throw new SQLException("[cdc-connector] canal connector not init.");
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
