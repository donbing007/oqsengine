package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.alibaba.google.common.base.Preconditions;
import com.xforceplus.ultraman.oqsengine.calculation.IDGenerator;
import java.util.List;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/14/21 2:37 PM
 * @since 1.8
 */
public class RedisIDGenerator implements IDGenerator {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Long nextId(String bizTag,int step) {
        Preconditions.checkArgument(step >= 1,"Step must great eq than 1");
        long result =  redissonClient.getAtomicLong(bizTag).addAndGet(step);
        return result;
    }

    @Override
    public List<Long> nextIds(String bizTag, int step, Integer batchSize) {
        List<Long> results = Lists.newArrayList();
        Preconditions.checkArgument(step >= 1,"Step must great eq than 1");
        for(int i=0;i<batchSize;i++) {
            results.add(redissonClient.getAtomicLong(bizTag).addAndGet(step));
        }
        return results;
    }

}
