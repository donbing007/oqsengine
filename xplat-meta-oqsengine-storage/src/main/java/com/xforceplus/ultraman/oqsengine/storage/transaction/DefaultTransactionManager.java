package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;

/**
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    private LongIdGenerator idGenerator;

    public DefaultTransactionManager(LongIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Transaction create() {
        long id = idGenerator.next();

        MultiLocalTransaction tx = new MultiLocalTransaction(id);

        this.bind(tx);

        return tx;
    }
}
