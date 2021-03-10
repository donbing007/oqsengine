package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.CdcErrorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * desc :
 * name : AbstractDevOpsExecutor
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public abstract class AbstractDevOpsExecutor<T, R> implements CdcErrorExecutor<T, R> {

    final Logger logger = LoggerFactory.getLogger(AbstractDevOpsExecutor.class);
    private String tableName;
    private DataSource dataSource;
    private long timeoutMs;

    public AbstractDevOpsExecutor(String tableName, DataSource dataSource) {
        this(tableName, dataSource, 0);
    }

    public AbstractDevOpsExecutor(String tableName, DataSource dataSource, long timeoutMs) {
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.timeoutMs = timeoutMs;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getTableName() {
        return tableName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    protected void checkTimeout(PreparedStatement statement) throws SQLException {
        if (getTimeoutMs() > 0) {
            statement.setQueryTimeout((int) (getTimeoutMs() / 1000));
        }
    }
}
