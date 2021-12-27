package com.xforceplus.ultraman.oqsengine.storage.executor.jdbc;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jdbc执行器统抽像.
 *
 * @param <R> 请求资源.
 * @param <T> 响应结果.
 * @author dongbin
 * @version 0.1 2020/11/6 14:13
 * @since 1.8
 */
public abstract class AbstractJdbcTaskExecutor<R, T> implements Executor<R, T> {

    final Logger logger = LoggerFactory.getLogger(AbstractJdbcTaskExecutor.class);
    private String tableName;
    private TransactionResource<Connection> resource;
    private long timeoutMs;

    public AbstractJdbcTaskExecutor(String tableName, TransactionResource<Connection> resource) {
        this(tableName, resource, 0);
    }

    /**
     * 构造实例.
     *
     * @param tableName 表名.
     * @param resource 事务资源.
     * @param timeoutMs 超时毫秒.
     */
    public AbstractJdbcTaskExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        this.tableName = tableName;
        this.resource = resource;
        this.timeoutMs = timeoutMs;
    }

    /**
     * 操作的当前表名.
     *
     * @return 表名.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 操作的超时时间(毫秒).
     *
     * @return 超时时间(毫秒).
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * 获取当前操作的事务资源.
     *
     * @return 事务资源.
     */
    public TransactionResource<Connection> getResource() {
        return resource;
    }

    /**
     * 获取日志管理器.
     *
     * @return 日志管理器.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * 检查是否可以设置超时时间.
     *
     * @param statement jDBC目标.
     */
    protected void checkTimeout(Statement statement) throws SQLException {
        if (getTimeoutMs() > 0) {
            statement.setQueryTimeout((int) (getTimeoutMs() / 1000));
        }
    }

    protected boolean[] executedUpdate(Statement st, boolean batch) throws SQLException {
        if (batch) {
            int[] flags = st.executeBatch();
            boolean[] results = new boolean[flags.length];
            for (int i = 0; i < flags.length; i++) {
                results[i] = flags[i] > 0 || flags[i] == Statement.SUCCESS_NO_INFO;
            }
            return results;
        } else {
            if (PreparedStatement.class.isInstance(st)) {
                return new boolean[] {
                    ((PreparedStatement) st).executeUpdate() > 0
                };
            } else {
                throw new UnsupportedOperationException(
                    "Single instance updates other than PreparedStatement are not supported.");
            }
        }
    }
}
