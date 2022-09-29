package com.xforceplus.ultraman.oqsengine.common.id;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内有效的有序递增 id 生成器.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 11:24
 * @since 1.8
 */
public class IncreasingOrderLongIdGenerator implements LongIdGenerator {

    private static final String DEFAULT_NS = "com.xforceplus.ultraman.oqsengine.default";

    private ConcurrentMap<String, AtomicLong> pool;
    private long initId;

    public IncreasingOrderLongIdGenerator() {
        this(0);
    }

    /**
     * 实例化.
     *
     * @param initId 初始值.
     */
    public IncreasingOrderLongIdGenerator(long initId) {
        initId = this.initId;
        pool = new ConcurrentHashMap<>();
        pool.put(DEFAULT_NS, new AtomicLong(initId));
    }

    @Override
    public Long next() {
        return next(DEFAULT_NS);
    }

    @Override
    public Long next(String nameSpace) {
        AtomicLong newLong = new AtomicLong(initId);
        AtomicLong old = pool.putIfAbsent(nameSpace, newLong);
        if (old == null) {
            return newLong.incrementAndGet();
        } else {
            return old.incrementAndGet();
        }
    }

    @Override
    public Long current() {
        return current(DEFAULT_NS);
    }

    @Override
    public Long current(String nameSpace) {
        AtomicLong old = pool.get(nameSpace);
        if (old == null) {
            return 0L;
        } else {
            return old.get();
        }
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
        reset(DEFAULT_NS);
    }

    @Override
    public void reset(String ns) {
        AtomicLong old = pool.putIfAbsent(ns, new AtomicLong(initId));
        if (old != null) {
            old.set(0);
        }
    }

    @Override
    public boolean supportNameSpace() {
        return true;
    }
}
