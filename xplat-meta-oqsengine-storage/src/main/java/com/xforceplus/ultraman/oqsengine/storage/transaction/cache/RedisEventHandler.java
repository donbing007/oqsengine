package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;

import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.event.EventType.TX_BEGIN;

/**
 * desc :
 * name : RedisEventHandler
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public class RedisEventHandler implements CacheEventHandler {

    final Logger logger = LoggerFactory.getLogger(RedisEventHandler.class);

    private String CUD_PAYLOAD_HASH_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.event.payload";

    private RedisClient redisClient;

    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    private Thread worker;

    private Queue<Long> expired = new LinkedBlockingQueue<>(512);

    //  3分钟过期
    public static final long EXPIRE_BUFFER_SECONDS = 3 * 60;
    private long expiredDuration = EXPIRE_BUFFER_SECONDS;

    private static final long CLOSE_WAIT_MAX_LOOP = 60;
    private static final long WAIT_DURATION = 1000;
    private static volatile boolean closed;

    public RedisEventHandler(RedisClient redisClient, ObjectMapper objectMapper, long expiredDuration) {
        this.redisClient = redisClient;

        this.objectMapper = objectMapper;

        if (this.redisClient == null) {
            throw new IllegalArgumentException("Invalid RedisClient instance.");
        }


        if (expiredDuration > 0) {
            this.expiredDuration = expiredDuration;
        }

        worker = new Thread(() -> {
            while (!closed) {
                try {
                    if (!expired.isEmpty()) {
                        Long txId = expired.poll();
                        if (null != txId) {
                            onEventEnd(txId);
                        }
                    } else {
                        Thread.sleep(WAIT_DURATION);
                    }

                } catch (Exception e) {
                    //  ignore

                }
            }
        });
    }

    @PostConstruct
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.event");

        closed = false;

        worker.start();
    }

    @PreDestroy
    public void destroy() {
        closed = true;

        if (null != worker) {
            waitForClosed(worker, CLOSE_WAIT_MAX_LOOP);
        }
    }

    @Override
    public boolean onEventCreate(Event<CachePayload> event) {
        return storage(event.payload().get().getTxId(), event.payload().get().getEntityId()
                , event.payload().get().getVersion(), event);
    }

    @Override
    public boolean onEventUpdate(Event<CachePayload> event) {
        return storage(event.payload().get().getTxId(), event.payload().get().getEntityId()
                , event.payload().get().getVersion(), event);
    }

    @Override
    public boolean onEventDelete(Event<CachePayload> event) {
        return storage(event.payload().get().getTxId(), event.payload().get().getEntityId()
                , event.payload().get().getVersion(), event);
    }

    @Override
    public boolean onEventBegin(long txId) {
        return true;
    }

    @Override
    public boolean onEventCommit(long txId, long maxOpNumber) {
        onEventEnd(txId);
        return true;
    }

    @Override
    public boolean onEventRollback(long txId) {
        onEventEnd(txId);
        return true;
    }

    private void onEventEnd(long txId) {
        String txIdStr = eventKeyGenerate(txId);
        try {
            if (!syncCommands.expire(txIdStr, expiredDuration)) {
                throw new RuntimeException(String.format("expired [%s] failed.", txIdStr));
            }
        } catch (Exception e) {
            expired.offer(txId);
            logger.warn(e.getMessage());
        }
    }


    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {

        if (null == id || null == version || invalidQueryEventType(eventType)) {
            //  查询对应txId的列表
            Map<String, String> result = syncCommands.hgetall(eventKeyGenerate(txId));

            return null == result || result.isEmpty() ?
                    Collections.emptyList() :
                        result.entrySet().stream()
                                .filter(e -> {
                                    return !e.getKey().equals(TX_BEGIN.name());
                                })
                                .map(Map.Entry::getValue)
                                .collect(Collectors.toList());
        }

        //  精确查询 txId + id + version 确定
        String rBuild = syncCommands.hget(eventKeyGenerate(txId), eventFieldGenerate(id, version, eventType));
        return null == rBuild || rBuild.isEmpty() ? Collections.emptyList() : Collections.singletonList(rBuild);
    }

    private boolean storage(long txId, long id, long version, Event<?> event) {
        try {
            String encodeJson = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(event).getBytes());

            return syncCommands.hset(eventKeyGenerate(txId), eventFieldGenerate(id, version, event.type().getValue()), encodeJson);
        } catch (Exception e) {
            logger.warn("storage error, [txId:{}-type:{}-id:{}-version:{}-message:{}], add to retry... ", txId, event.type(), id, version, e.getMessage());

            return false;
        }
    }

    private String eventFieldGenerate(long id, long version, int eventType) {
        return  String.format("%d.%d.%d", id, version, eventType);
    }

    private String eventKeyGenerate(long txId) {
        return eventKeyGenerate(Long.toString(txId));
    }

    private String eventKeyGenerate(String txIdString) {
        return String.format("%s.%s", CUD_PAYLOAD_HASH_KEY_PREFIX, txIdString);
    }

    private boolean invalidQueryEventType(Integer eventType) {
        return null == eventType ||
                (eventType != EventType.ENTITY_BUILD.getValue() &&
                        eventType != EventType.ENTITY_REPLACE.getValue() &&
                        eventType != EventType.ENTITY_DELETE.getValue());
    }

    private void waitForClosed(Thread thread, long maxWaitLoops) {
        for (int i = 0; i < maxWaitLoops; i++) {
            if (!thread.isAlive()) {
                logger.info("wait for loops [{}] and thread closed successful.", i);
                break;
            }
            sleep(WAIT_DURATION);
        }
        logger.info("reach max wait loops [{}] and thread will be force-closed.", maxWaitLoops);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
