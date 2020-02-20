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

    private AtomicLong id = new AtomicLong(0);

    @Override
    public Long next() {
        return id.incrementAndGet();
    }
}
