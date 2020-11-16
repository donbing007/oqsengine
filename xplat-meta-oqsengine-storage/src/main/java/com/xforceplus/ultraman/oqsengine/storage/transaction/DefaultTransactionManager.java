package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;

/**
 * 默认的事务管理器.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    private LongIdGenerator txIdGenerator;
    private LongIdGenerator commitIdGenerator;

    public DefaultTransactionManager(LongIdGenerator txIdGenerator, LongIdGenerator commitIdGenerator) {
        this(3000, txIdGenerator, commitIdGenerator);
    }

    public DefaultTransactionManager(int survivalTimeMs, LongIdGenerator txIdGenerator, LongIdGenerator commitIdGenerator) {
        super(survivalTimeMs);
        this.txIdGenerator = txIdGenerator;
        this.commitIdGenerator = commitIdGenerator;

        if (!txIdGenerator.isPartialOrder()) {
            throw new IllegalArgumentException(
                "The generator of the transaction number requires a partial order implementation.");
        }

        if (!this.commitIdGenerator.isContinuous() && !this.commitIdGenerator.isPartialOrder()) {
            throw new IllegalArgumentException(
                "The commit number of the transaction needs to support continuous and partial ID generation implementations.");
        }
    }

    @Override
    public Transaction doCreate() {
        long txId = txIdGenerator.next();

        Transaction tx = new MultiLocalTransaction(txId, commitIdGenerator);

        return tx;
    }
}
