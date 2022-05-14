package com.xforceplus.ultraman.oqsengine.storage.transaction.hint;

/**
 * 默认事务提示实现.
 *
 * @author dongbin
 * @version 0.1 2022/5/13 17:39
 * @since 1.8
 */
public class DefaultTransactionHint implements TransactionHint {

    private boolean rollback = false;
    private boolean canWaitCommitSync = false;

    public DefaultTransactionHint() {
    }

    @Override
    public boolean isRollback() {
        return rollback;
    }

    @Override
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    @Override
    public boolean isCanWaitCommitSync() {
        return canWaitCommitSync;
    }

    @Override
    public void setCanWaitCommitSync(boolean canWaitCommitSync) {
        if (!this.canWaitCommitSync) {
            this.canWaitCommitSync = canWaitCommitSync;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTransactionHint{");
        sb.append("canWaitCommitSync=").append(canWaitCommitSync);
        sb.append(", rollback=").append(rollback);
        sb.append('}');
        return sb.toString();
    }
}
