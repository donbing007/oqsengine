package com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl;

import com.alibaba.google.common.cache.CacheBuilder;
import com.alibaba.google.common.cache.CacheLoader;
import com.alibaba.google.common.cache.LoadingCache;
import com.alibaba.google.common.collect.Maps;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.ResetModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.TimeDelay;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

/**
 * .
 *
 * @author leo
 * @version 0.1 2021/12/8 4:27 下午
 * @since 1.8
 */
public class RedisCacheImpl implements IDGenerator {

    private static final Long MAX_SIZE = 20000L;
    private static final Long MAX_CACHE_TIME_IN_SECONDS = 3600L;

    private PatternParserManager patternParserManager;
    protected SegmentService segmentService;
    private RedissonClient redissonClient;
    private String bizType;
    private RAtomicLong counter;
    protected LoadingCache<String, RAtomicLong> cache;

    /**
     * 构造函数.
     */
    public RedisCacheImpl(SegmentService segmentService, RedissonClient redissonClient, String bizType,
                          PatternParserManager patternParserManager) {
        this.segmentService = segmentService;
        this.redissonClient = redissonClient;
        this.bizType = bizType;
        this.patternParserManager = patternParserManager;
        SegmentInfo segmentInfo = segmentService.getSegmentInfo(this.bizType);
        if (segmentInfo.getResetable().equals(ResetModel.UNRESETABLE.value())) {
            counter = redissonClient.getAtomicLong(bizType);
        } else {
            cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
                .expireAfterWrite(MAX_CACHE_TIME_IN_SECONDS, TimeUnit.SECONDS)
                .build(new CacheLoader<String, RAtomicLong>() {
                    @Override
                    public RAtomicLong load(String key) throws SQLException {
                        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
                        return atomicLong;
                    }
                });
        }
    }


    @Override
    public String nextId() {
        SegmentInfo segmentInfo = segmentService.getSegmentInfo(this.bizType);
        Long id = 0L;
        if (segmentInfo.getResetable().equals(ResetModel.UNRESETABLE.value())) {
            id = counter.incrementAndGet();
        } else {
            String key = String.format("%s-%s", bizType, this.patternParserManager.parse(segmentInfo.getPattern(), 0L));
            RAtomicLong counter = null;
            try {
                counter = cache.get(key);
                id = counter.incrementAndGet();
                if (counter.remainTimeToLive() == -1L) {
                    TimeDelay delay =
                        DatePatternParser.getMaxExpireDate(segmentInfo.getPattern());
                    if (delay != null) {
                        counter.expire(delay.getDelay(), delay.getTimeUnit());
                    }
                }
            } catch (Exception e) {
                String errorMsg = String.format("Get IdModel : %s failed!", bizType);
                throw new IDGeneratorException(errorMsg);
            }
        }
        return patternParserManager.parse(segmentInfo.getPattern(), id);
    }

    @Override
    public List<String> nextIds(Integer batchSize) {
        return null;
    }
}
