package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micrometer.core.instrument.Metrics;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提交号状态管理者.
 * 提交号的状态将有3种,UNKNOWN,READY,NOT_READY.
 * 正常的提交号处于READY或者UN_READY中,但是如果检查的提交号没有保存过那么使用如下策略.
 * 阻塞最多检查的次数为30次,然后将自动变成READY.虽然实际此提交号并没有状态改变过.
 * 这样防止检查方一直阻塞在一个不处于受管状态的提交号.
 * 被淘汰的提交号状态就自动变为UNKNOWN.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:33
 * @since 1.8
 */
public class CommitIdStatusServiceImpl implements CommitIdStatusService, Lifecycle {

    final Logger logger = LoggerFactory.getLogger(CommitIdStatusServiceImpl.class);

    private static final long DEFAULT_UNKNOWN_LIMIT_NUMBER = 30;
    private static final String DEFAULT_COMMITIDS_KEY = "com.xforceplus.ultraman.oqsengine.status.commitids";
    private static final String DEFAULT_COMMITID_STATUS_KEY_PREFIX =
        "com.xforceplus.ultraman.oqsengine.status.commitid.";
    private static final String COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX =
        "com.xforceplus.ultraman.oqsengine.status.commitid.unknown.number.";

    /**
     * 接收2个KEY2个参数.
     * KEYS:
     * 1 未同步提交号列表key.
     * 2 当前处理提交号的状态key前辍.
     * ARGV:
     * 1 提交号,纯数字.
     * 2 需要设置的状态字符.
     * 脚本会判断是否已经淘汰,不理将设置为指定的状态.
     * 返回1表示设置成功,0表示没有设置.
     * 相当于执行这样一个过程.
     * long commitId = 123;
     * CommitStatus newStatus = CommitStatus.READY;
     * String value = command.get("com.xforceplus.ultraman.oqsengine.status.commitid." + commitId);
     * if (CommitStatus.ELIMINATION.getSymbol() != value) {
     * command.zadd("com.xforceplus.ultraman.oqsengine.status.commitids", (double) commitId, Long.toString(commitId));
     * command.set("com.xforceplus.ultraman.oqsengine.status.commitid." + commitId, newStatus.getSymbol());
     * return true;
     * } else {
     * return false;
     * }
     */
    private static String SAVE_LUA_SCRIPT = String.format(
        "local statusKey = KEYS[2]..ARGV[1]"
            + "local status = redis.call('get', statusKey);"
            + "if status ~= '%s' "
            + "then "
            + "redis.call('set', statusKey, ARGV[2]);"
            + "redis.call('zadd', KEYS[1], ARGV[1], ARGV[1]);"
            + "return 1;"
            + "else "
            + "return 0;"
            + "end;", CommitStatus.ELIMINATION.getSymbol());

    /**
     * 循环删除提交号的LUA脚本.
     * KEYS:
     * 1. 未同步提交号队列KEY.
     * 2. 提交号状态KEY前辍.
     * 3. 记录触发UNKNOWN状态isReady检查的次数KEY.
     * ARGV: 数量不定,表示需要淘汰的提交号列表.
     */
    private static String OBSOLETE_LUA_SCRIPT = String.format(
        "for i=1, #ARGV, 1 do "
            + "local statusKey = KEYS[2]..ARGV[i];"
            + "local unknownKey = KEYS[3]..ARGV[i];"
            + "redis.call('del', unknownKey);"
            + "redis.call('set', statusKey, '%s','EX', %d);"
            + "redis.call('zrem', KEYS[1], ARGV[i]);"
            + "end;return true", CommitStatus.ELIMINATION.getSymbol(), 60 * 60
    );

    /**
     * 小于等于此值的判定为无效的commitid.
     */
    public static final long INVALID_COMMITID = 0;

    @Resource(name = "redisClientState")
    private RedisClient redisClient;

    @Resource
    private RedisLuaScriptWatchDog redisLuaScriptWatchDog;

    public Timer timer;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    private String commitidsKey;

    private String commitidStatusKeyPrefix;

    private String saveLuaScriptSha;
    private String obsoleteLuaScriptSha;

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

    /**
     * 实例化.
     *
     * @param commitidsKey            提交号key.
     * @param commitIdStatusKeyPreifx 提交号状态key前辍.
     * @param limitUnknownNumber      最大未知状态提交号数量.
     */
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
    public void init() throws Exception {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.sync.commitid");


        if (redisLuaScriptWatchDog != null) {
            saveLuaScriptSha = redisLuaScriptWatchDog.watch(SAVE_LUA_SCRIPT);
            obsoleteLuaScriptSha = redisLuaScriptWatchDog.watch(OBSOLETE_LUA_SCRIPT);
        } else {
            saveLuaScriptSha = syncCommands.scriptLoad(SAVE_LUA_SCRIPT);
            obsoleteLuaScriptSha = syncCommands.scriptLoad(OBSOLETE_LUA_SCRIPT);
        }

        unSyncCommitIdSize = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_COUNT_TOTAL, new AtomicLong(size()));
        unSyncCommitIdMin = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_MIN, new AtomicLong(size()));
        unSyncCommitIdMax = Metrics.gauge(
            MetricsDefine.UN_SYNC_COMMIT_ID_MAX, new AtomicLong(size()));

        logger.info("Use {} as the key for the list of commit Numbers.", commitidsKey);
        logger.info("Use {} as the prefix key for the commit number status.", commitidStatusKeyPrefix);
        logger.info("Use {} as the prefix key for the commit number status unknown.",
            COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX);

        timer = new Timer("commit-update-metrics", true);
        timer.schedule(new UpdateMetricsTask(), 1000L, 6000L);
    }

    @PreDestroy
    public void destroy() throws Exception {
        timer.cancel();
        syncConnect.close();
    }

    @Override
    public boolean save(long commitId, boolean ready) {
        if (commitId <= INVALID_COMMITID) {
            return false;
        }

        String[] keys = {
            commitidsKey,
            commitidStatusKeyPrefix,
        };
        boolean result = syncCommands.evalsha(
            saveLuaScriptSha,
            ScriptOutputType.BOOLEAN,
            keys,
            Long.toString(commitId),
            ready ? CommitStatus.READY.getSymbol() : CommitStatus.NOT_READY.getSymbol());

        if (logger.isDebugEnabled()) {
            CommitStatus logStatus = ready ? CommitStatus.READY : CommitStatus.NOT_READY;
            if (result) {
                logger.debug("The commit number {} was successfully saved with the status {}.",
                    commitId, logStatus.name());
            } else {
                logger.debug("The submission number {} is obsolete and will not be saved.", commitId);
            }
        }

        return result;
    }

    @Override
    public boolean isReady(long commitId) {
        if (commitId <= INVALID_COMMITID) {
            logger.warn("Invalid COMMITID {}.", commitId);
            return true;
        }

        CommitStatus status = getStatus(commitId);

        if (CommitStatus.READY == status) {

            return true;

        } else {

            /*
             * UNKNOWN的检查次数达到阀值后将被直接认定为ready.
             */
            if (CommitStatus.UNKNOWN == status) {

                String unknownNumberKey = COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX + commitId;
                long newNumber = syncCommands.incr(unknownNumberKey);
                if (newNumber > limitUnknownNumber) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(
                            "The commit number {} check is always UNKNOWN, the check number reaches the threshold {} and the ready state is automatically changed.",
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

    /**
     * 同时判断多个提交号是否就绪.
     *
     * @param commitIds 提交号列表.
     * @return true 就绪, false 没有就绪.
     */
    public boolean[] isReady(long[] commitIds) {
        if (commitIds == null || commitIds.length == 0) {
            return new boolean[0];
        }

        int len = commitIds.length;
        CommitStatus[] commitStatuses = getStatus(commitIds);
        boolean[] statues = new boolean[commitStatuses.length];
        for (int i = 0; i < len; i++) {
            statues[i] = CommitStatus.READY == commitStatuses[i];
        }

        return statues;
    }

    @Override
    public void ready(long commitId) {
        if (commitId <= INVALID_COMMITID) {
            return;
        }

        changeStatus(commitId, CommitStatus.READY);
    }

    @Override
    public long[] getUnreadiness() {
        return Arrays.stream(getAll()).filter(commitid -> !isReady(commitid)).toArray();
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

        String[] keys = {
            commitidsKey,
            commitidStatusKeyPrefix,
            COMMITID_STATUS_UNKNOWN_NUMBER_PREFIX,
        };
        String[] ids = Arrays.stream(commitIds).mapToObj(commitId -> Long.toString(commitId)).toArray(String[]::new);
        syncCommands.evalsha(obsoleteLuaScriptSha, ScriptOutputType.BOOLEAN, keys, ids);

        if (logger.isDebugEnabled()) {
            logger.debug("The commit`s number {} has been eliminated.", Arrays.toString(commitIds));
        }
    }

    @Override
    public void obsoleteAll() {
        obsolete(getAll());
    }

    @Override
    public boolean isObsolete(long commitId) {
        CommitStatus status = getStatus(commitId);
        return CommitStatus.ELIMINATION == status;
    }

    public void setLimitUnknownNumber(long limitUnknownNumber) {
        this.limitUnknownNumber = limitUnknownNumber;
    }

    // 获取提交号状态.
    private CommitStatus getStatus(long commitId) {
        String statusKey = String.format("%s%d", commitidStatusKeyPrefix, commitId);
        String value = syncCommands.get(statusKey);

        CommitStatus status = CommitStatus.getInstance(value);

        if (logger.isDebugEnabled()) {
            logger.debug("Check that the status of the commit number {} is {}.", commitId, status.name());
        }

        return status;
    }

    // 批量获取提交号状态.
    private CommitStatus[] getStatus(long[] commitIds) {
        int len = commitIds.length;
        if (len == 0) {
            return new CommitStatus[0];
        }

        String[] keys =
            Arrays.stream(commitIds)
                .mapToObj(c -> String.format("%s%d", commitidStatusKeyPrefix, c)).toArray(String[]::new);

        // 帮助定位key所在的下标.
        Map<String, Integer> keyPos = new HashMap<>(MapUtils.calculateInitSize(len));
        for (int i = 0; i < len; i++) {
            keyPos.put(keys[i], i);
        }

        List<KeyValue<String, String>> keyValues = syncCommands.mget(keys);
        CommitStatus[] statuses = new CommitStatus[len];
        KeyValue<String, String> kv;
        int pos;
        for (int i = 0; i < keyValues.size(); i++) {
            kv = keyValues.get(i);
            pos = keyPos.get(kv.getKey());

            if (kv.hasValue()) {
                statuses[pos] = CommitStatus.getInstance(kv.getValue());
            } else {
                statuses[pos] = CommitStatus.READY;
            }
        }

        return statuses;
    }

    // 修改提交号状态.
    private void changeStatus(long commitId, CommitStatus status) {
        String statusKey = commitidStatusKeyPrefix + commitId;
        syncCommands.set(statusKey, status.getSymbol());
    }

    private enum CommitStatus {
        UNKNOWN("U"),
        NOT_READY("N"),
        READY("R"),
        ELIMINATION("E");

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

    /**
     * 更新指标定时任务.
     */
    private class UpdateMetricsTask extends TimerTask {

        @Override
        public void run() {
            try {
                unSyncCommitIdSize.set(size());
                Optional<Long> commitId = getMin();
                if (commitId.isPresent()) {
                    unSyncCommitIdMin.set(commitId.get());
                } else {
                    unSyncCommitIdMin.set(-1);
                }

                commitId = getMax();
                if (commitId.isPresent()) {
                    unSyncCommitIdMax.set(commitId.get());
                } else {
                    unSyncCommitIdMax.set(0);
                }
            } catch (Throwable ex) {
                //do not care.
            }
        }
    }
}
