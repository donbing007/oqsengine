package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;

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
    private CommitIdStatusService commitIdStatusService;
    private boolean waitCommitSync;

    public DefaultTransactionManager(
        LongIdGenerator txIdGenerator,
        LongIdGenerator commitIdGenerator,
        CommitIdStatusService commitIdStatusService) {
        this(3000, txIdGenerator, commitIdGenerator, commitIdStatusService, false);
    }

    public DefaultTransactionManager(
        LongIdGenerator txIdGenerator,
        LongIdGenerator commitIdGenerator,
        CommitIdStatusService commitIdStatusService,
        boolean waitCommitSync) {
        this(3000, txIdGenerator, commitIdGenerator, commitIdStatusService, waitCommitSync);
    }

    public DefaultTransactionManager(
        int survivalTimeMs,
        LongIdGenerator txIdGenerator,
        LongIdGenerator commitIdGenerator,
        CommitIdStatusService commitIdStatusService,
        boolean waitCommitSync) {

        super(survivalTimeMs);
        this.txIdGenerator = txIdGenerator;
        this.commitIdGenerator = commitIdGenerator;
        this.commitIdStatusService = commitIdStatusService;
        this.waitCommitSync = waitCommitSync;

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

        if (waitCommitSync) {
            return new MultiLocalTransaction(txId, commitIdGenerator, commitIdStatusService);
        } else {
            return new MultiLocalTransaction(txId, commitIdGenerator, commitIdStatusService, 0);
        }
    }
}
