package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 累加器的默认实现.
 *
 * @author dongbin
 * @version 0.1 2020/12/11 15:30
 * @since 1.8
 */
public class DefaultTransactionAccumulator implements TransactionAccumulator {

    private AtomicLong buildTimes = new AtomicLong(0);
    private AtomicLong replaceTimes = new AtomicLong(0);
    private AtomicLong deleteTimes = new AtomicLong(0);

    @Override
    public void accumulateBuild() {
        buildTimes.incrementAndGet();
    }

    @Override
    public void accumulateReplace() {
        replaceTimes.incrementAndGet();
    }

    @Override
    public void accumulateDelete() {
        deleteTimes.incrementAndGet();
    }

    @Override
    public long getBuildTimes() {
        return buildTimes.get();
    }

    @Override
    public long getReplaceTimes() {
        return replaceTimes.get();
    }

    @Override
    public long getDeleteTimes() {
        return deleteTimes.get();
    }

    @Override
    public void reset() {
        buildTimes.set(0);
        replaceTimes.set(0);
        deleteTimes.set(0);
    }
}
