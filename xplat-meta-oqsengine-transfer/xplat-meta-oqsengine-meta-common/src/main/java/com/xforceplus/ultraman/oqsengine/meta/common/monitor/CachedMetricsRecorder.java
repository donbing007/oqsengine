package com.xforceplus.ultraman.oqsengine.meta.common.monitor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CachedMetricsRecorder implements MetricsRecorder {

    final Logger logger = LoggerFactory.getLogger(CachedMetricsRecorder.class);

    /**
     * use key to record the syncLogs.
     */
    private Cache<String, Map<String, MetricsLog.Message>> syncLogs;

    /**
     * use key to record the errorLogs.
     */
    private Cache<String, Map<String, MetricsLog.Message>> errorLogs;

    private static final int DEFAULT_MAX_CACHE_SIZE = 1024;
    private static final int DEFAULT_CACHE_EXPIRE = 86400;

    public CachedMetricsRecorder() {
        this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
    }

    /**
     * 新的实例.
     */
    public CachedMetricsRecorder(int maxCacheSize, int cacheExpire) {
        if (0 >= maxCacheSize) {
            maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
        }
        if (0 >= cacheExpire) {
            cacheExpire = DEFAULT_CACHE_EXPIRE;
        }

        syncLogs = CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize)
            .expireAfterWrite(cacheExpire, TimeUnit.SECONDS)
            .build();

        errorLogs = CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize)
            .expireAfterWrite(cacheExpire, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public void error(String key, String code, String message) {
        logger.warn("code: {}, key: {}, message: {}", code, key, message);
        try {
            Map<String, MetricsLog.Message> r = errorLogs.get(key, LinkedHashMap::new);
            r.put(code, new MetricsLog.Message(message));
        } catch (ExecutionException e) {
            logger.warn("record message error.");
        }
    }

    @Override
    public void info(String key, String code, String message) {
        logger.info("code: {}, key: {}, message: {}", code, key, message);
        try {
            Map<String, MetricsLog.Message> r = syncLogs.get(key, LinkedHashMap::new);
            r.put(code, new MetricsLog.Message(message));
        } catch (ExecutionException e) {
            logger.warn("record message error.");
        }
    }

    @Override
    public List<MetricsLog> showLogs(MetricsLog.ShowType showType) {

        if (null != showType) {
            switch (showType) {
                case INFO:
                    return MetricsLog.toMetricsLogs(syncLogs.asMap());
                case ERROR:
                    return MetricsLog.toMetricsLogs(errorLogs.asMap());
                default: {
                }
            }
        }
        List<MetricsLog> metricsLogs = new ArrayList<>();
        metricsLogs.addAll(MetricsLog.toMetricsLogs(syncLogs.asMap()));
        metricsLogs.addAll(MetricsLog.toMetricsLogs(errorLogs.asMap()));

        return metricsLogs;
    }
}
