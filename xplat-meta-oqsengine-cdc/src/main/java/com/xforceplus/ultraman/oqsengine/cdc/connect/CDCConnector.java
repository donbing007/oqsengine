package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DEFAULT_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DEFAULT_SUBSCRIBE_FILTER;


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

    private String subscribeFilter = DEFAULT_SUBSCRIBE_FILTER;

    private int batchSize = DEFAULT_BATCH_SIZE;

    protected CanalConnector canalConnector;

    private boolean isClosed = true;

    public void shutdown() {
        try {
            if (canalConnector.checkValid()) {
                close();
            }
        } catch (Exception e) {
            logger.warn("shutdown error.");
            e.printStackTrace();
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
            canalConnector.subscribe(subscribeFilter);
            logger.info("connect to canal server...");
            isClosed = false;
        }
    }

    /**
     * 关闭canal连接
     */
    public void close() {
        if (null != canalConnector && !isClosed) {
            try {
                logger.error("close canal connector...");
                //  关闭连接CanalServer
                canalConnector.disconnect();
            } catch (Exception e) {
                logger.error("close error, ex : {}", e.getMessage());
            } finally {
                isClosed = true;
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
        throw new SQLException("canal connector not init.");
    }

    public void setSubscribeFilter(String subscribeFilter) {
        this.subscribeFilter = subscribeFilter;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
