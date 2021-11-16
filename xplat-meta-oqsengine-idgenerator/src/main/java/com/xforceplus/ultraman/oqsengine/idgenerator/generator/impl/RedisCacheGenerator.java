package com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.IDResult;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.ResultCode;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * Generator redis号段缓存实现.
 *
 * @author leo
 * @version 0.1 2021/5/13 11:59
 * @since 1.8
 */
public class RedisCacheGenerator implements IDGenerator {


    protected SegmentService segmentService;
    private RedissonClient redissonClient;
    private RBucket<SegmentId> current;
    private RBucket<SegmentId> next;
    private RAtomicLong isLoadingNext;
    private String bizType;
    private ExecutorService executorService;

    /**
     * constructor.
     *
     * @param bizType         bizType
     * @param segmentService  segmentService
     * @param executorService executorService
     * @param redissonClient  redissonClient
     */
    public RedisCacheGenerator(String bizType, SegmentService segmentService, ExecutorService executorService,
                               RedissonClient redissonClient) {
        this.segmentService = segmentService;
        this.redissonClient = redissonClient;
        this.current = redissonClient.getBucket(String.format("%s:%s", bizType, "current"));
        this.next = redissonClient.getBucket(String.format("%s:%s", bizType, "next"));
        this.isLoadingNext = redissonClient.getAtomicLong(String.format("%s:%s", bizType, "loadingNext"));
        this.executorService = executorService;
        this.bizType = bizType;
        loadCurrent(bizType);
    }

    /**
     * Get the current segment by bizType.
     *
     * @param bizType bizType
     */
    public synchronized void loadCurrent(String bizType) {
        RLock lock = redissonClient.getLock(bizType);
        lock.lock();
        try {
            if (current.get() == null || !current.get().useful()) {
                if (next == null || next.get() == null) {
                    SegmentId segmentId = querySegmentId(bizType);
                    this.current.set(segmentId);
                } else {
                    current.set(next.get());
                    next.set(null);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private SegmentId querySegmentId(String bizType) {
        String message = null;
        try {
            SegmentId segmentId = segmentService.getNextSegmentId(bizType);
            if (segmentId != null) {
                return segmentId;
            }
        } catch (Exception e) {
            message = e.getMessage();
            throw new IDGeneratorException(
                String.format("error query segment: bizType: %s error message :%s ", bizType, message));
        }
        throw new IDGeneratorException(
            String.format("error query segment: bizType: %s error message :%s ", bizType, message));
    }

    /**
     * Load the next segment.
     */
    public void loadNext() {
        if (next.get() == null && isLoadingNext.get() == 0) {
            RLock lock = redissonClient.getLock(this.bizType);
            lock.lock();
            try {
                if (next.get() == null && isLoadingNext.get() == 0) {
                    isLoadingNext.incrementAndGet();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                next.set(querySegmentId(bizType));
                            } finally {
                                isLoadingNext.decrementAndGet();
                            }
                        }
                    });
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Reset the segment.
     *
     * @param result result if next Id
     */
    public synchronized void resetBizType(IDResult result) {
        RLock lock = redissonClient.getLock(this.bizType);
        lock.lock();
        try {
            String message = null;
            if (current != null) {
                current.set(null);
            }
            if (next != null) {
                next.set(null);
            }
            String patternKey = result.getPatternKey();
            try {
                segmentService.resetSegment(bizType, patternKey);
            } catch (Throwable throwable) {
                message = throwable.getMessage();
            }
            if (message != null) {
                throw new IDGeneratorException("Error reset the segment: " + message);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String nextId() {
        while (true) {
            if (current.get() == null) {
                loadCurrent(bizType);
                continue;
            }
            IDResult result = null;
            SegmentId currentValue;
            SegmentId nextValue;
            //do {
            //    currentValue = current.get();
            //    nextValue = currentValue.clone();
            //    result = nextValue.nextId();
            //} while (!current.compareAndSet(currentValue, nextValue));
            RLock lock = redissonClient.getLock(this.bizType);
            lock.lock();
            try {
                currentValue = current.get();
                nextValue = currentValue.clone();
                result = nextValue.nextId();
                current.set(nextValue);
            } finally {
                lock.unlock();
            }

            if (result.getCode() == ResultCode.OVER) {
                loadCurrent(bizType);
            } else if (result.getCode() == ResultCode.RESET) {
                resetBizType(result);
            } else {
                if (result.getCode() == ResultCode.LOADING && isLoadingNext.get() == 0) {
                    loadNext();
                }
                return result.getId();
            }
        }

    }

    @Override
    public List<String> nextIds(Integer batchSize) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            String id = nextId();
            ids.add(id);
        }
        return ids;
    }
}
