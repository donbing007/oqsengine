package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * master库执行器统抽像.
 *
 * @param <RES> 请求资源.
 * @param <REQ> 响应结果.
 * @author dongbin
 * @version 0.1 2020/11/6 14:13
 * @since 1.8
 */
public abstract class AbstractMasterExecutor<RES, REQ> implements Executor<RES, REQ> {

    final Logger logger = LoggerFactory.getLogger(AbstractMasterExecutor.class);
    private String tableName;
    private TransactionResource<Connection> resource;
    private long timeoutMs;

    public AbstractMasterExecutor(String tableName, TransactionResource<Connection> resource) {
        this(tableName, resource, 0);
    }

    public AbstractMasterExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        this.tableName = tableName;
        this.resource = resource;
        this.timeoutMs = timeoutMs;
    }

    public String getTableName() {
        return tableName;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public TransactionResource<Connection> getResource() {
        return resource;
    }

    public Logger getLogger() {
        return logger;
    }

    protected void checkTimeout(PreparedStatement statement) throws SQLException {
        if (getTimeoutMs() > 0) {
            statement.setQueryTimeout((int) (getTimeoutMs() / 1000));
        }
    }
}
