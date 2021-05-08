package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 抽像实现.提供了索引名称和操作资源.
 *
 * @param <R> 请求.
 * @param <T> 响应.
 * @author dongbin
 * @version 0.1 2021/3/3 11:43
 * @since 1.8
 */
public abstract class AbstractIndexExecutor<R, T> implements Executor<R, T> {

    private String indexName;
    private TransactionResource transactionResource;
    private long timeoutMs;

    public AbstractIndexExecutor(String indexName, TransactionResource transactionResource) {
        this(indexName, transactionResource, 0);
    }

    /**
     * 实例化.
     *
     * @param indexName 索引名称.
     * @param transactionResource 事务资源.
     * @param timeoutMs 超时毫秒.
     */
    public AbstractIndexExecutor(String indexName, TransactionResource transactionResource, long timeoutMs) {
        this.indexName = indexName;
        this.transactionResource = transactionResource;
        this.timeoutMs = timeoutMs;
    }

    public String getIndexName() {
        return indexName;
    }

    public TransactionResource<Connection> getTransactionResource() {
        return transactionResource;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    protected void checkTimeout(Statement statement) throws SQLException {
        if (getTimeoutMs() > 0) {
            statement.setQueryTimeout((int) (getTimeoutMs() / 1000));
        }
    }
}
