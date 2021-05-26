package com.xforceplus.ultraman.oqsengine.idgenerator.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * master库执行器统抽像.
 *
 * @param <RES> 请求资源.
 * @param <REQ> 响应结果.
 * @author dongbin
 * @version 0.1 2020/11/6 14:13
 * @since 1.8
 */
public abstract class AbstractSegmentExecutor<RES, REQ> implements Executor<RES, REQ> {

    final Logger logger = LoggerFactory.getLogger(AbstractSegmentExecutor.class);
    private String tableName;
    private DataSource dataSource;
    private long timeoutMs;

    public AbstractSegmentExecutor(String tableName, DataSource resource) {
        this(tableName, resource, 0);
    }

    public AbstractSegmentExecutor(String tableName, DataSource resource, long timeoutMs) {
        this.tableName = tableName;
        this.dataSource = resource;
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
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 检查是否可以设置超时时间.
     *
     * @param statement jDBC目标.
     * @throws SQLException
     */
    protected void checkTimeout(Statement statement) throws SQLException {
        if (getTimeoutMs() > 0) {
            statement.setQueryTimeout((int) (getTimeoutMs() / 1000));
        }
    }
}
