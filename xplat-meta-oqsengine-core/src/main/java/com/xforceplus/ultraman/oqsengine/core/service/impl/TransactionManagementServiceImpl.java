package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/2/18 14:34
 * @since 1.8
 */
public class TransactionManagementServiceImpl implements TransactionManagementService {

    @Resource
    private TransactionManager transactionManager;

    @Override
    public long begin() throws SQLException {
        long txId = transactionManager.create().id();

        // 创建事务,但是当前不需要使用取消绑定.
        transactionManager.unbind();

        return txId;
    }

    @Override
    public void commit() throws SQLException {

        Transaction tx = transactionManager.getCurrent();
        tx.commit();

        transactionManager.finish(tx);
    }

    @Override
    public void rollback() throws SQLException {

        Transaction tx = transactionManager.getCurrent();
        tx.rollback();

        transactionManager.finish(tx);

    }
}
