package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.DefaultTransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;

/**
 * 执行器的提示默认实现.
 * @author dongbin
 * @version 0.1 2020/6/23 14:41
 * @since 1.8
 */
public class DefaultExecutorHint implements ExecutorHint {

    private boolean rollback = false;
    private TransactionAccumulator accumulator;

    public DefaultExecutorHint() {
        this(null);
    }

    public DefaultExecutorHint(TransactionAccumulator accumulator) {
        this.accumulator = accumulator;
        if (this.accumulator == null) {
            this.accumulator = new DefaultTransactionAccumulator();
        }
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
    public TransactionAccumulator getAccumulator() {
        return accumulator;
    }
}
