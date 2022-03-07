package com.xforceplus.ultraman.oqsengine.cdc.connect;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DEFAULT_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_RECONNECT_TIMES_PER_CONNECTIONS;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CDC 连接器抽像实现.
 *
 * @author xujia 2020/11/5
 * @since 1.8
 */
public abstract class AbstractCDCConnector {

    final Logger logger = LoggerFactory.getLogger(AbstractCDCConnector.class);

    private int batchSize = DEFAULT_BATCH_SIZE;

    protected CanalConnector canalConnector;

    private boolean isClosed = true;

    //  canal的基本信息
    protected String connectString;
    protected String destination;
    protected String userName;
    protected String password;


    /**
     * 构造器.
     */
    public AbstractCDCConnector(String connectString, String destination, String userName, String password) {
        this.connectString = connectString;
        this.destination = destination;
        this.userName = userName;
        this.password = password;
    }

    public boolean isMaxRetry(int times) {
        return times >= MAX_RECONNECT_TIMES_PER_CONNECTIONS;
    }

    public abstract void init();

    /**
     * 关闭.只有当连接合法时才会关闭链接.否则什么都不做.
     */
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
     * 打开canal连接.
     */
    public void open() {
        if (null != canalConnector) {
            //  连接CanalServer
            canalConnector.connect();
            //  订阅destination
            canalConnector.subscribe();
            logger.info("[cdc-connector] connect to canal server...");
            isClosed = false;
        }
    }

    /**
     * 无条件关闭canal连接.
     */
    public void close() {
        if (null != canalConnector && !isClosed) {
            try {
                logger.info("[cdc-connector] close canal connector...");
                //  关闭连接CanalServer
                canalConnector.disconnect();
            } catch (Exception e) {
                logger.error("[cdc-connector] close error, ex : {}", e.getMessage());
            } finally {
                isClosed = true;
            }
        }
    }

    /**
     * 回滚.
     */
    public void rollback() {
        if (null != canalConnector) {
            canalConnector.rollback();
        }
    }

    /**
     * 回应.
     */
    public void ack(long batchId) throws SQLException {
        if (null == canalConnector) {
            notInitException();
        }
        canalConnector.ack(batchId);
    }

    /**
     * 不自动确认获取消息.
     *
     * @return 消息实例.
     * @throws SQLException 获取发生异常.
     */
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
