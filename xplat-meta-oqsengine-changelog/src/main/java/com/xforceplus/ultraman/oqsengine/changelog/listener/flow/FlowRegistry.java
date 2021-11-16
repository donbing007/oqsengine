package com.xforceplus.ultraman.oqsengine.changelog.listener.flow;

import akka.stream.ActorMaterializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 * flow queue for rate limit
 */
public class FlowRegistry {

    private ActorMaterializer mat;

    private Logger logger = LoggerFactory.getLogger(FlowRegistry.class);

    private Integer timeoutMili;

    LoadingCache<String, QueueFlow> cache;

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    /**
     * 构造实例.
     */
    public FlowRegistry(ActorMaterializer mat, Integer mili) {
        this.mat = mat;
        this.timeoutMili = mili;

        cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(timeoutMili, TimeUnit.MILLISECONDS)
            .removalListener(new RemovalListener<String, QueueFlow>() {
                @Override
                public void onRemoval(@Nullable String s, @Nullable QueueFlow queueFlow,
                                      @NonNull RemovalCause removalCause) {
                    logger.info("Flow for {} is over due to time expiry", s);
                    if (queueFlow != null) {
                        queueFlow.close();
                    }
                }
            })
            .build(key -> {
                logger.info("Flow for {} is setup", key);
                return new QueueFlow(key, mat);
            });

        scheduledExecutorService.scheduleAtFixedRate(() -> cache.cleanUp(), 2L, 10L, TimeUnit.SECONDS);
    }

    public <T> QueueFlow<T> flow(String name) {
        //contextService
        return cache.get(name);
    }
}
