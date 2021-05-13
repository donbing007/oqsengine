package com.xforceplus.ultraman.oqsengine.idgenerator.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于普通 JDBC connection 规范的资源实现.
 * 强制事务以READ_COMMITTED运行.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 21:57
 * @since 1.8
 */
public class SegmentTransactionResource extends AbstractConnectionTransactionResource {

    final Logger logger = LoggerFactory.getLogger(SegmentTransactionResource.class);

    public SegmentTransactionResource(
        String key,
        Connection conn,
        boolean autocommit,
        String tableName) throws SQLException {
        super(key, conn, autocommit);
    }

    @Override
    public TransactionResourceType type() {
        return TransactionResourceType.MASTER;
    }


}
