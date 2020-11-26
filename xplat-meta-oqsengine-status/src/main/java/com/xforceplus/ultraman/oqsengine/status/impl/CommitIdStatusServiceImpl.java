package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.common.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 提交号状态管理者.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:33
 * @since 1.8
 */
public class CommitIdStatusServiceImpl implements CommitIdStatusService {

    final Logger logger = LoggerFactory.getLogger(CommitIdStatusServiceImpl.class);

    private static final String DEFAULT_KEY = "com.xforceplus.ultraman.oqsengine.status.commitid";

    @Resource
    private RedisClient redisClient;

    @Resource
    private ResourceLocker locker;

    private StatefulRedisConnection<String, String> syncConnect;

    private StatefulRedisConnection<String, String> asyncConnect;

    private RedisCommands<String, String> syncCommands;

    private RedisAsyncCommands<String, String> asyncCommands;

    private String key;

    private AtomicLong unSyncCommitIdSize;

    public CommitIdStatusServiceImpl() {
        this(DEFAULT_KEY);
    }

    public CommitIdStatusServiceImpl(String key) {
        this.key = key;
        if (this.key == null || this.key.isEmpty()) {
            throw new IllegalArgumentException("The KEY is invalid.");
        }

    }

    @PostConstruct
    public void init() {
        if (locker == null) {
            throw new IllegalStateException("Invalid ResourceLocker.");
        }
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.sync.commitid");

        asyncConnect = redisClient.connect();
        asyncCommands = asyncConnect.async();
        asyncCommands.setAutoFlushCommands(false);
        asyncCommands.clientSetname("oqs.async.commitid");

        unSyncCommitIdSize = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_COUNT_TOTAL, new AtomicLong(size()));


        logger.info("Use {} as the key for the list of submission Numbers.", key);

    }

    @PreDestroy
    public void destroy() {
        syncConnect.close();
        asyncConnect.close();
    }

    @Override
    public long save(long commitId) {
        return save(commitId, null);
    }

    @Override
    public long save(long commitId, Runnable act) {

        String target = Long.toString(commitId);

        locker.lock(target);
        try {
            if (act != null) {
                act.run();
            }
            syncCommands.zadd(key, (double) commitId, target);
        } finally {
            locker.unlock(target);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Save the new commit number {}.", commitId);
        }

        updateMetrics();

        return commitId;

    }

    @Override
    public Optional<Long> getMin() {
        List<String> ids = syncCommands.zrange(key, 0, 0);
        if (ids.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("The current minimum commit number not obtained.");
            }

            return Optional.empty();
        } else {
            // 首个元素
            final int first = 0;

            if (logger.isDebugEnabled()) {
                logger.debug("The minimum commit number to get to is {}.", ids.get(first));
            }

            return Optional.of(Long.parseLong(ids.get(first)));
        }
    }

    @Override
    public Optional<Long> getMax() {
        List<String> ids = syncCommands.zrevrange(key, 0, 0);
        if (ids.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("The current maximum commit number not obtained.");
            }

            return Optional.empty();
        } else {

            // 首个元素
            final int first = 0;

            if (logger.isDebugEnabled()) {
                logger.debug("The maximum commit number to get to is {}.", ids.get(first));
            }

            return Optional.of(Long.parseLong(ids.get(first)));
        }
    }

    @Override
    public long[] getAll() {
        List<String> ids = syncCommands.zrange(key, 0, -1);
        return ids.parallelStream().mapToLong(id -> Long.parseLong(id)).toArray();
    }

    @Override
    public long size() {
        return syncCommands.zcard(key);
    }

    @Override
    public void obsolete(long... commitIds) {

        List<RedisFuture<Long>> futures = new ArrayList<>(commitIds.length);
        String target;
        for (long id : commitIds) {
            target = Long.toString(id);
            locker.lock(target);
            try {

                futures.add(asyncCommands.zrem(key, target));

            } finally {
                if (!locker.unlock(target)) {
                    logger.error("Unable to unlock, target submission number is {}.", target);
                }
            }
        }

        asyncCommands.flushCommands();
        LettuceFutures.awaitAll(1, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));

        if (logger.isDebugEnabled()) {
            logger.debug("The commit`s number {} has been eliminated.", Arrays.toString(commitIds));
        }
        updateMetrics();


    }

    private void updateMetrics() {
        CompletableFuture.runAsync(() -> unSyncCommitIdSize.set(size()));
    }
}
