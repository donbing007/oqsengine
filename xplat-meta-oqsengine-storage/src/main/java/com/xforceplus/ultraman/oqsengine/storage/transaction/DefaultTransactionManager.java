package com.xforceplus.ultraman.oqsengine.storage.transaction;

/**
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    @Override
    public Transaction create() {
        //TODO: id generator. by dongbin 2020/02/15
        long id = 0;

        MultiLocalTransaction tx = new MultiLocalTransaction(id);

        this.bind(tx);

        return tx;
    }
}
