package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;

import java.util.concurrent.TimeUnit;

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
    private EventBus eventBus;

    private DefaultTransactionManager(int survivalTimeMs) {
        super(survivalTimeMs);
    }

    @Override
    public Transaction doCreate(String msg) {
        long txId = txIdGenerator.next();

        MultiLocalTransaction.Builder builder = MultiLocalTransaction.Builder.aMultiLocalTransaction()
            .withId(txId)
            .withCommitIdStatusService(commitIdStatusService)
            .withLongIdGenerator(commitIdGenerator)
            .withEventBus(eventBus)
            .withMsg(msg);

        if (waitCommitSync) {
            builder.withMaxWaitCommitIdSyncMs(TimeUnit.MINUTES.toMillis(1));
        } else {
            builder.withMaxWaitCommitIdSyncMs(0);
        }

        return builder.build();
    }


    /**
     * builder
     */
    public static final class Builder {
        private LongIdGenerator txIdGenerator;
        private LongIdGenerator commitIdGenerator;
        private CommitIdStatusService commitIdStatusService;
        private boolean waitCommitSync = true;
        private EventBus eventBus;
        private int survivalTimeMs = 30000;

        private Builder() {
        }

        public static Builder aDefaultTransactionManager() {
            return new Builder();
        }

        public Builder withTxIdGenerator(LongIdGenerator txIdGenerator) {
            this.txIdGenerator = txIdGenerator;
            return this;
        }

        public Builder withCommitIdGenerator(LongIdGenerator commitIdGenerator) {
            this.commitIdGenerator = commitIdGenerator;
            return this;
        }

        public Builder withCommitIdStatusService(CommitIdStatusService commitIdStatusService) {
            this.commitIdStatusService = commitIdStatusService;
            return this;
        }

        public Builder withWaitCommitSync(boolean waitCommitSync) {
            this.waitCommitSync = waitCommitSync;
            return this;
        }

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Builder withSurvivalTimeMs(int survivalTimeMs) {
            this.survivalTimeMs = survivalTimeMs;
            return this;
        }

        public DefaultTransactionManager build() {
            DefaultTransactionManager defaultTransactionManager = new DefaultTransactionManager(survivalTimeMs);
            defaultTransactionManager.eventBus = this.eventBus;
            defaultTransactionManager.waitCommitSync = this.waitCommitSync;
            defaultTransactionManager.commitIdStatusService = this.commitIdStatusService;
            defaultTransactionManager.txIdGenerator = this.txIdGenerator;
            defaultTransactionManager.commitIdGenerator = this.commitIdGenerator;

            if (!txIdGenerator.isPartialOrder()) {
                throw new IllegalArgumentException(
                    "The generator of the transaction number requires a partial order implementation.");
            }

            if (!this.commitIdGenerator.isContinuous() && !this.commitIdGenerator.isPartialOrder()) {
                throw new IllegalArgumentException(
                    "The commit number of the transaction needs to support continuous and partial ID generation implementations.");
            }
            return defaultTransactionManager;
        }
    }
}
