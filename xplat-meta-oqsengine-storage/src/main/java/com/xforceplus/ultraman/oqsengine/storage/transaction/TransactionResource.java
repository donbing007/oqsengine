package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/2/15 21:51
 * @since 1.8
 */
public interface TransactionResource<V> {

    Object key();

    V value();

    void commit() throws SQLException;

    void rollback() throws SQLException;

    void destroy() throws SQLException;
}
