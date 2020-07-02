package com.xforceplus.ultraman.oqsengine.sdk.util.flow;

import akka.stream.ActorMaterializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * flow queue for rate limit
 */
public class FlowRegistry {

    private ActorMaterializer mat;

    private Logger logger = LoggerFactory.getLogger(FlowRegistry.class);

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    LoadingCache<String, QueueFlow> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, QueueFlow>() {
                @Override
                public void onRemoval(@Nullable String s, @Nullable QueueFlow queueFlow, @NonNull RemovalCause removalCause) {
                    logger.info("Flow for {} is over due to time expiry", s);
                    if(queueFlow != null) {
                        queueFlow.close();
                    }
                }
            })
            .build(key -> {
                logger.info("Flow for {} is setup", key);
                return new QueueFlow(key, mat);
            });


    public FlowRegistry(ActorMaterializer mat) {
        this.mat = mat;
        scheduledExecutorService.scheduleAtFixedRate(() -> cache.cleanUp(), 2L, 10L, TimeUnit.SECONDS);
    }

    public <T> QueueFlow<T> flow(String name) {
        //contextService
        return cache.get(name);
    }
}
