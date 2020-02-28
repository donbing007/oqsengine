package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的事务管理器.
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    final Logger logger = LoggerFactory.getLogger(DefaultTransactionManager.class);

    private LongIdGenerator idGenerator;

    public DefaultTransactionManager(LongIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Transaction doCreate() {
        long id = idGenerator.next();

        MultiLocalTransaction tx = new MultiLocalTransaction(id);

        this.bind(tx);

        return tx;
    }
}
