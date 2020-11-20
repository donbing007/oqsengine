package com.xforceplus.ultraman.oqsengine.status.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author dongbin
 * @version 0.1 2020/11/16 15:45
 * @since 1.8
 */
public class CDCStatusServiceImpl implements CDCStatusService {

    private static final String DEFAULT_KEY = "com.xforceplus.ultraman.oqsengine.status.CDCStatusServiceImpl";
    private static final String HEART_BEAT_KEY = "com.xforceplus.ultraman.oqsengine.status.CDCStatusServiceImpl.heartBeat";

    @Resource
    private RedisClient redisClient;
    @Resource
    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> connect;

    private String key;

    private String heartBeatKey;

    public CDCStatusServiceImpl() {
        this(DEFAULT_KEY, HEART_BEAT_KEY);
    }

    public CDCStatusServiceImpl(String key, String heartBeat) {
        this.key = key;
        if (this.key == null || this.key.isEmpty()) {
            throw new IllegalArgumentException("The KEY is invalid.");
        }

        this.heartBeatKey = heartBeat;
        if (this.heartBeatKey == null || this.heartBeatKey.isEmpty()) {
            throw new IllegalArgumentException("The heartBeatKey is invalid.");
        }
    }

    @PostConstruct
    public void init() {
        connect = redisClient.connect();
        RedisCommands<String, String> commands = connect.sync();
        commands.clientSetname("oqs.cdc");
    }

    @PreDestroy
    public void destroy() {
        connect.close();
    }

    @Override
    public boolean heartBeat(long heartBeat) {
        RedisCommands<String, String> commands = connect.sync();
        String res = commands.set(heartBeatKey, Long.toString(heartBeat));
        return "OK".equals(res);
    }

    @Override
    public boolean save(CDCMetrics cdcMetrics) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(cdcMetrics);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        RedisCommands<String, String> commands = connect.sync();
        String res = commands.set(key, json);
        return "OK".equals(res);
    }

    @Override
    public Optional<CDCMetrics> get() {
        RedisCommands<String, String> commands = connect.sync();
        String json = commands.get(key);
        if (json == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(objectMapper.readValue(json, CDCMetrics.class));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }
}
