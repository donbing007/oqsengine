package com.xforceplus.ultraman.oqsengine.status.impl;

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

    private static final String DEFAULT_COMMITIDS_KEY = "com.xforceplus.ultraman.oqsengine.status.commitids";
    private static final String DEFAULT_COMMITID_STATUS_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.status.commitid";

    /**
     * 小于等于此值的判定为无效的commitid.
     */
    private static final long INVALID_COMMITID = 0;

    @Resource
    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> syncConnect;

    private StatefulRedisConnection<String, String> asyncConnect;

    private RedisCommands<String, String> syncCommands;

    private RedisAsyncCommands<String, String> asyncCommands;

    private String commitidsKey;

    private String commitidStatusKeyPrefix;

    private AtomicLong unSyncCommitIdSize;

    public CommitIdStatusServiceImpl() {
        this(DEFAULT_COMMITIDS_KEY, DEFAULT_COMMITID_STATUS_KEY_PREFIX);
    }

    public CommitIdStatusServiceImpl(String commitidsKey, String commitIdStatusKeyPreifx) {
        this.commitidsKey = commitidsKey;
        if (this.commitidsKey == null || this.commitidsKey.isEmpty()) {
            throw new IllegalArgumentException("The commits key is invalid.");
        }

        this.commitidStatusKeyPrefix = commitIdStatusKeyPreifx;
        if (this.commitidStatusKeyPrefix == null || this.commitidStatusKeyPrefix.isEmpty()) {
            throw new IllegalArgumentException("The commit status key is invalid.");
        }
    }

    @PostConstruct
    public void init() {
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


        logger.info("Use {} as the key for the list of submission Numbers.", commitidsKey);

    }

    @PreDestroy
    public void destroy() {
        syncConnect.close();
        asyncConnect.close();
    }

    @Override
    public long save(long commitId, boolean ready) {
        if (commitId <= INVALID_COMMITID) {
            return commitId;
        }

        String target = Long.toString(commitId);
        String statusKey = commitidStatusKeyPrefix + "." + target;

        syncCommands.zadd(commitidsKey, (double) commitId, target);
        if (ready) {
            syncCommands.set(statusKey, CommitStatus.READY.getSymbol());
        } else {
            syncCommands.set(statusKey, CommitStatus.NOT_READY.getSymbol());
        }

        updateMetrics();

        return commitId;
    }

    @Override
    public boolean isReady(long commitId) {
        if (commitId <= INVALID_COMMITID) {
            return true;
        }

        String target = Long.toString(commitId);
        String statusKey = commitidStatusKeyPrefix + "." + target;
        String value = syncCommands.get(statusKey);

        CommitStatus status = CommitStatus.getInstance(value);

        if (CommitStatus.READY == status) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void ready(long commitId) {
        if (commitId <= INVALID_COMMITID) {
            return;
        }

        String target = Long.toString(commitId);
        String statusKey = commitidStatusKeyPrefix + "." + target;
        syncCommands.set(statusKey, CommitStatus.READY.getSymbol());
    }

    @Override
    public Optional<Long> getMin() {
        List<String> ids = syncCommands.zrange(commitidsKey, 0, 0);
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
        List<String> ids = syncCommands.zrevrange(commitidsKey, 0, 0);
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
        List<String> ids = syncCommands.zrange(commitidsKey, 0, -1);
        return ids.parallelStream().mapToLong(id -> Long.parseLong(id)).toArray();
    }

    @Override
    public long size() {
        return syncCommands.zcard(commitidsKey);
    }

    @Override
    public void obsolete(long... commitIds) {

        if (commitIds.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No submission number needs to be eliminated.");
            }
            return;
        }

        List<RedisFuture<Long>> futures = new ArrayList<>(commitIds.length);
        String target;
        String statusKey;
        for (long id : commitIds) {
            target = Long.toString(id);
            statusKey = commitidStatusKeyPrefix + "." + target;

            // 清理未同步列表中的提交号,表示此提交号已经同步.
            futures.add(asyncCommands.zrem(commitidsKey, target));
            // 清理未同步列表状态.
            futures.add(asyncCommands.del(statusKey));
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

    private enum CommitStatus {
        UNKNOWN("U"),
        NOT_READY("N"),
        READY("R");

        private String symbol;

        public String getSymbol() {
            return symbol;
        }

        CommitStatus(String symbol) {
            this.symbol = symbol;
        }

        public static CommitStatus getInstance(String symbol) {
            for (CommitStatus status : CommitStatus.values()) {
                if (status.getSymbol().equals(symbol)) {
                    return status;
                }
            }

            return UNKNOWN;
        }
    }
}
