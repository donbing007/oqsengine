package com.xforceplus.ultraman.oqsengine.status.impl;

import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 提交号状态管理者.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:33
 * @since 1.8
 */
public class CommitIdStatusServiceImpl implements CommitIdStatusService {

    private static final String DEFAULT_KEY = "com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService";

    @Resource
    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> connect;

    private String key;

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
        connect = redisClient.connect();
        RedisCommands<String, String> commands = connect.sync();
        commands.clientSetname("oqs.commitid");
    }

    @PreDestroy
    public void destroy() {
        connect.close();
    }

    @Override
    public long save(long commitId) {
        RedisCommands<String, String> commands = connect.sync();
        commands.zadd(key, (double) commitId, Long.toString(commitId));
        return commitId;
    }

    @Override
    public Optional<Long> getMin() {
        RedisCommands<String, String> commands = connect.sync();
        List<String> ids = commands.zrange(key, 0, 0);
        if (ids.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Long.parseLong(ids.get(0)));
        }
    }

    @Override
    public Optional<Long> getMax() {
        RedisCommands<String, String> commands = connect.sync();
        List<String> ids = commands.zrevrange(key, 0, 0);
        if (ids.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Long.parseLong(ids.get(0)));
        }
    }

    @Override
    public long[] getAll() {
        RedisCommands<String, String> commands = connect.sync();
        List<String> ids = commands.zrange(key, 0, Long.MAX_VALUE);
        return ids.stream().mapToLong(id -> Long.parseLong(id)).toArray();
    }

    @Override
    public long size() {
        RedisCommands<String, String> commands = connect.sync();
        return commands.zcard(key);
    }

    @Override
    public long obsolete(long commitId) {
        RedisCommands<String, String> commands = connect.sync();
        long size = commands.zrem(key, Long.toString(commitId));
        if (size == 1) {
            return commitId;
        } else {
            return -1;
        }
    }

    @Override
    public void obsolete(long... commitIds) {
        RedisCommands<String, String> commands = connect.sync();

        commands.multi();
        Arrays.stream(commitIds).mapToObj(id -> Long.toString(id)).forEach(id -> commands.zrem(key, id));
        commands.exec();
    }
}
