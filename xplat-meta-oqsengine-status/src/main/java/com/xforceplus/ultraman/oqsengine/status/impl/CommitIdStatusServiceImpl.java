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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 提交号状态管理者.
 * 提交号的状态将有3种,UNKNOWN,READY,UN_READY.
 * 正常的提交号处于READY或者UN_READY中,但是如果检查的提交号没有保存过那么使用如下策略.
 * 阻塞最多检查的次数为10次,然后将自动变成READY.虽然实际此提交号并没有状态改变过.
 * 这样防止检查方一直阻塞在一个不处于受管状态的提交号.
 * 被淘汰的提交号状态就自动变为UNKNOWN.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:33
 * @since 1.8
 */
public class CommitIdStatusServiceImpl implements CommitIdStatusService {

    final Logger logger = LoggerFactory.getLogger(CommitIdStatusServiceImpl.class);

    private static final long DEFAULT_UNKNOWN_LIMIT_NUMBER = 30;
    private static final String DEFAULT_COMMITIDS_KEY = "com.xforceplus.ultraman.oqsengine.status.commitids";
    private static final String DEFAULT_COMMITID_STATUS_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.status.commitid.";
    private static final String COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX =
        "com.xforceplus.ultraman.oqsengine.status.commitid.unknown.number.";

    /**
     * 小于等于此值的判定为无效的commitid.
     */
    public static final long INVALID_COMMITID = 0;

    @Resource
    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> syncConnect;

    private StatefulRedisConnection<String, String> asyncConnect;

    private RedisCommands<String, String> syncCommands;

    private RedisAsyncCommands<String, String> asyncCommands;

    private String commitidsKey;

    private String commitidStatusKeyPrefix;

    private long limitUnknownNumber;

    private AtomicLong unSyncCommitIdSize;
    private AtomicLong unSyncCommitIdMin;
    private AtomicLong unSyncCommitIdMax;

    public CommitIdStatusServiceImpl() {
        this(DEFAULT_COMMITIDS_KEY, DEFAULT_COMMITID_STATUS_KEY_PREFIX);
    }

    public CommitIdStatusServiceImpl(String commitidsKey, String commitIdStatusKeyPreifx) {
        this(commitidsKey, commitIdStatusKeyPreifx, DEFAULT_UNKNOWN_LIMIT_NUMBER);
    }

    public CommitIdStatusServiceImpl(String commitidsKey, String commitIdStatusKeyPreifx, long limitUnknownNumber) {
        this.commitidsKey = commitidsKey;
        if (this.commitidsKey == null || this.commitidsKey.isEmpty()) {
            throw new IllegalArgumentException("The commits key is invalid.");
        }

        this.commitidStatusKeyPrefix = commitIdStatusKeyPreifx;
        if (this.commitidStatusKeyPrefix == null || this.commitidStatusKeyPrefix.isEmpty()) {
            throw new IllegalArgumentException("The commit status key is invalid.");
        }

        this.limitUnknownNumber = limitUnknownNumber;
        final long minUnknownNumber = 1;
        if (this.limitUnknownNumber < minUnknownNumber) {
            this.limitUnknownNumber = DEFAULT_UNKNOWN_LIMIT_NUMBER;
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

        unSyncCommitIdMin = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_MIN, new AtomicLong(getMin()));

        unSyncCommitIdMax = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_MAX, new AtomicLong(getMax()));

        logger.info("Use {} as the key for the list of commit Numbers.", commitidsKey);
        logger.info("Use {} as the prefix key for the commit number status.", commitidStatusKeyPrefix);
        logger.info("Use {} as the prefix key for the commit number status unknown.",
            COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX);

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
        String statusKey = commitidStatusKeyPrefix + target;

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
            logger.warn("Invalid COMMITID {}.", commitId);
            return true;
        }

        String target = Long.toString(commitId);
        String statusKey = commitidStatusKeyPrefix + target;
        String value = syncCommands.get(statusKey);

        CommitStatus status = CommitStatus.getInstance(value);

        if (logger.isDebugEnabled()) {
            logger.debug("Check that the status of the submission number {} is {}.", commitId, status.name());
        }

        if (CommitStatus.READY == status) {

            return true;

        } else {

            /**
             * UNKNOWN的检查次数达到阀值后将被直接认定为ready.
             */
            if (CommitStatus.UNKNOWN == status) {

                String unknownNumberKey = COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX + commitId;
                long newNumber = syncCommands.incr(unknownNumberKey);
                if (newNumber > limitUnknownNumber) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("The commit number {} check is always UNKNOWN, " +
                                "the check number reaches the threshold {} and the ready state is automatically changed.",
                            commitId, limitUnknownNumber);
                    }
                    return true;
                } else {
                    return false;
                }

            } else {

                return false;

            }

        }
    }

    @Override
    public void ready(long commitId) {
        if (commitId <= INVALID_COMMITID) {
            return;
        }

        String target = Long.toString(commitId);
        String statusKey = commitidStatusKeyPrefix + target;
        syncCommands.set(statusKey, CommitStatus.READY.getSymbol());
    }

    @Override
    public long[] getUnreadiness() {
        return Arrays.stream(getAll()).filter(commitid -> !isReady(commitid)).toArray();
    }

    @Override
    public long getMin() {
        List<String> ids = syncCommands.zrange(commitidsKey, 0, 0);
        if (ids.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("The current minimum commit number not obtained.");
            }

            return -1;
        } else {
            // 首个元素
            final int first = 0;

            if (logger.isDebugEnabled()) {
                logger.debug("The minimum commit number to get to is {}.", ids.get(first));
            }

            return Long.parseLong(ids.get(first));
        }
    }

    @Override
    public long getMax() {
        List<String> ids = syncCommands.zrevrange(commitidsKey, 0, 0);
        if (ids.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("The current maximum commit number not obtained.");
            }

            return -1;
        } else {

            // 首个元素
            final int first = 0;

            if (logger.isDebugEnabled()) {
                logger.debug("The maximum commit number to get to is {}.", ids.get(first));
            }

            return Long.parseLong(ids.get(first));
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
        String unknownKey;
        for (long id : commitIds) {
            target = Long.toString(id);
            statusKey = commitidStatusKeyPrefix + target;
            unknownKey = COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX + target;

            // 清理未同步列表中的提交号,表示此提交号已经同步.
            futures.add(asyncCommands.zrem(commitidsKey, target));
            // 清理未同步列表状态.
            futures.add(asyncCommands.del(statusKey));
            // 清理可能的UNKNOWN次数记录.
            futures.add(asyncCommands.del(unknownKey));
        }

        asyncCommands.flushCommands();
        LettuceFutures.awaitAll(1, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));

        if (logger.isDebugEnabled()) {
            logger.debug("The commit`s number {} has been eliminated.", Arrays.toString(commitIds));
        }
        updateMetrics();
    }

    @Override
    public void obsoleteAll() {
        obsolete(getAll());
    }

    @Override
    public boolean isObsolete(long commitId) {
        String statusKey = commitidStatusKeyPrefix + commitId;
        return syncCommands.exists(statusKey) <= 0;
    }

    public void setLimitUnknownNumber(long limitUnknownNumber) {
        this.limitUnknownNumber = limitUnknownNumber;
    }

    private void updateMetrics() {
        CompletableFuture.runAsync(() -> {
            unSyncCommitIdSize.set(size());
            unSyncCommitIdMin.set(getMin());
            unSyncCommitIdMax.set(getMax());
        });
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
