package com.xforceplus.ultraman.oqsengine.storage.transaction.resource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于 java.sql.Connection 的资源.
 *
 * @author dongbin
 * @version 0.1 2020/5/19 10:41
 * @since 1.8
 */
public abstract class AbstractConnectionTransactionResource extends AbstractTransactionResource<Connection> {

    private boolean autoCommit;

    public AbstractConnectionTransactionResource(String key, Connection value, boolean autoCommit) throws SQLException {
        super(key, value);
        this.autoCommit = autoCommit;
        if (value().getAutoCommit() != autoCommit) {
            value().setAutoCommit(autoCommit);
        }
    }

    @Override
    public void commit() throws SQLException {
        value().commit();
    }

    @Override
    public void rollback() throws SQLException {
        value().rollback();
    }

    @Override
    public void destroy() throws SQLException {
        value().close();
    }

    @Override
    public boolean isDestroyed() throws SQLException {
        return value().isClosed();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
}
