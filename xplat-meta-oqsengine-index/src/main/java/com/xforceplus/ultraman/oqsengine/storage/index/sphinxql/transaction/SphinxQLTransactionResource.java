package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * sphinxQL 相关的资源管理器.
 *
 * @author dongbin
 * @version 0.1 2020/2/28 17:25
 * @since 1.8
 */
public class SphinxQLTransactionResource extends AbstractConnectionTransactionResource {

    public SphinxQLTransactionResource(String key, Connection conn, boolean autocommit) throws SQLException {
        super(key, conn, autocommit);
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.INDEX;
    }

}
