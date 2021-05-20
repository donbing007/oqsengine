//package com.xforceplus.ultraman.oqsengine.idgenerator.transaction;
//
//import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
//import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//
///**
// * @author dongbin
// * @version 0.1 2020/11/12 15:26
// * @since 1.8
// */
//public class SegmentTransactionResourceFactory implements TransactionResourceFactory<Connection> {
//
//    private String tableName;
//
//    public SegmentTransactionResourceFactory(String tableName) {
//        this.tableName = tableName;
//    }
//
//    @Override
//    public TransactionResource build(String key, Connection resource, boolean autocommit) throws SQLException {
//        return new SegmentTransactionResource(key, resource, autocommit, tableName);
//    }
//}
