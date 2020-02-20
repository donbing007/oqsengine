package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * entity 管理服务实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:12
 * @since 1.8
 */
public class EntityManagementServiceImpl implements EntityManagementService {

    @Resource
    private LongIdGenerator idGenerator;

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Override
    public long build(IEntity entity) throws SQLException {
        long newId = idGenerator.next();


        boolean localTx;
        Transaction tx = transactionManager.getCurrent();
        if (tx == null) {
            tx = transactionManager.create();
            localTx = true;
        } else {
            localTx = false;
        }
        try {

            masterStorage.build(entity);
            indexStorage.build(entity);

        } catch (SQLException ex) {
            if (localTx) {
                tx.rollback();
                transactionManager.finish(tx);
            }
            throw ex;
        }

        if (localTx) {
            tx.commit();
            transactionManager.finish(tx);
        }

        return newId;
    }

    @Override
    public void replace(IEntity entity) throws SQLException {

        masterStorage.replace(entity);
        indexStorage.replace(entity);
    }

    @Override
    public void delete(IEntity entity) throws SQLException {

        masterStorage.delete(entity);
        indexStorage.delete(entity);
    }
}
