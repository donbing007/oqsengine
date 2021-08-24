package com.xforceplus.ultraman.oqsengine.common.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内有效的有序递增 id 生成器.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 11:24
 * @since 1.8
 */
public class IncreasingOrderLongIdGenerator implements LongIdGenerator {

    private AtomicLong id;

    public IncreasingOrderLongIdGenerator() {
        this(0);
    }

    public IncreasingOrderLongIdGenerator(long initId) {
        id = new AtomicLong(initId);
    }

    @Override
    public Long next() {
        return id.incrementAndGet();
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isPartialOrder() {
        return true;
    }

    @Override
    public void reset() {
        id.set(0);
    }
}
