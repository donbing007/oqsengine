package com.xforceplus.ultraman.oqsengine.status.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.TimeGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dongbin
 * @version 0.1 2020/11/16 15:45
 * @since 1.8
 */
public class CDCStatusServiceImpl implements CDCStatusService {

    private static final String DEFAULT_CDC_METRICS_KEY = "com.xforceplus.ultraman.oqsengine.status.cdc.metrics";
    private static final String DEFAULT_HEART_BEAT_KEY = "com.xforceplus.ultraman.oqsengine.status.cdc.heartBeat";
    private static final String DEFAULT_NOT_READY_KEY = "com.xforceplus.ultraman.oqsengine.status.cdc.notReady";
    private static final String DEFAULT_CDC_ACK_METRICS_KEY = "com.xforceplus.ultraman.oqsengine.status.cdc.ack";

    private static final String DEFAULT_CDC_SKIPS_KEY = "com.xforceplus.ultraman.oqsengine.status.cdc.skips";

    final Logger logger = LoggerFactory.getLogger(CDCStatusServiceImpl.class);

    @Resource
    private RedisClient redisClient;
    @Resource
    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> connect;

    private String metricsKey;

    private String ackKey;

    private String heartBeatKey;

    private String notReadyKey;

    private long lastHeartBeatValue = -1;
    private long lastNotReadyValue = -1;
    private AtomicLong cdcSyncTime = new AtomicLong(0);
    private TimeGauge.Builder<AtomicLong> cdcSyncTimeGauge;
    private AtomicLong cdcExecutedCountGauge;
    private AtomicLong cdcNotReadyCommitIdGauge;

    public CDCStatusServiceImpl() {
        this(DEFAULT_CDC_METRICS_KEY, DEFAULT_CDC_ACK_METRICS_KEY, DEFAULT_HEART_BEAT_KEY, DEFAULT_NOT_READY_KEY);
    }

    public CDCStatusServiceImpl(String metricsKey, String ack, String heartBeat, String notReady) {
        this.metricsKey = metricsKey;
        if (this.metricsKey == null || this.metricsKey.isEmpty()) {
            throw new IllegalArgumentException("The cdc status metrics is invalid.");
        }

        this.ackKey = ack;
        if (this.ackKey == null || this.ackKey.isEmpty()) {
            throw new IllegalArgumentException("The ack key is invalid.");
        }

        this.heartBeatKey = heartBeat;
        if (this.heartBeatKey == null || this.heartBeatKey.isEmpty()) {
            throw new IllegalArgumentException("The heartBeatKey is invalid.");
        }

        this.notReadyKey = notReady;
        if (this.notReadyKey == null || this.notReadyKey.isEmpty()) {
            throw new IllegalArgumentException("The notReadyKey is invalid.");
        }
    }

    @PostConstruct
    public void init() {
        connect = redisClient.connect();
        RedisCommands<String, String> commands = connect.sync();
        commands.clientSetname("oqs.cdc");

        cdcSyncTimeGauge =
            TimeGauge.builder(
                MetricsDefine.CDC_SYNC_DELAY_LATENCY_SECONDS, cdcSyncTime, TimeUnit.MILLISECONDS, AtomicLong::get);

        cdcExecutedCountGauge =
            Metrics.gauge(MetricsDefine.CDC_SYNC_EXECUTED_COUNT, new AtomicLong(0));

        cdcSyncTimeGauge.register(Metrics.globalRegistry);

        cdcNotReadyCommitIdGauge =
                Metrics.gauge(MetricsDefine.CDC_NOT_READY_COMMIT, new AtomicLong(lastNotReadyValue));
    }

    @PreDestroy
    public void destroy() {
        connect.close();
    }

    @Override
    public boolean heartBeat() {
        RedisCommands<String, String> commands = connect.sync();
        try {
            commands.incr(heartBeatKey);
        } catch (RedisCommandExecutionException ex) {
            if ("ERR increment or decrement would overflow".equals(ex.getMessage())) {
                //表示已经溢出了,最多64个bit.进行回卷.
                commands.set(heartBeatKey, Long.toString(0));
            }
        }

        return true;
    }

    @Override
    public void notReady(long commitId) {
        cdcNotReadyCommitIdGauge.set(commitId);
    }

    @Override
    public boolean isAlive() {
        RedisCommands<String, String> commands = connect.sync();
        String value = commands.get(heartBeatKey);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No heartbeat data for object found. Default is CDC alive.");
            }
            return true;
        }
        long now = Long.parseLong(value);
        /**
         * 如果当前值和最后值不同,那么表示CDC仍然存活.
         * lastHeartBeatValue 默认等于-1,心跳从0开始.
         */
        if (logger.isDebugEnabled()) {
            logger.debug("The current heartbeat is {}, and the final heartbeat is {}.", now, lastHeartBeatValue);
        }
        try {
            if (now != lastHeartBeatValue) {
                return true;
            } else {
                return false;
            }
        } finally {
            lastHeartBeatValue = now;
        }
    }

    @Override
    public boolean saveUnCommit(CDCMetrics cdcMetrics) {
        return save(cdcMetrics, metricsKey);
    }

    @Override
    public Optional<CDCMetrics> getUnCommit() {
        return get(metricsKey, CDCMetrics.class);
    }

    @Override
    public boolean saveAck(CDCAckMetrics ackMetrics) {
        try {
            return save(ackMetrics, ackKey);
        } finally {
            cdcSyncTime.set(ackMetrics.getTotalUseTime());

            cdcExecutedCountGauge.set(ackMetrics.getExecuteRows());
        }
    }

    @Override
    public Optional<CDCAckMetrics> getAck() {
        return get(ackKey, CDCAckMetrics.class);
    }

    private boolean save(Object obj, String key) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        RedisCommands<String, String> commands = connect.sync();
        String res = commands.set(key, json);

        return "OK".equals(res);

    }

    private <T> Optional<T> get(String key, Class<T> clazz) {
        RedisCommands<String, String> commands = connect.sync();
        String json = commands.get(key);
        if (json == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(objectMapper.readValue(json, clazz));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }
}
