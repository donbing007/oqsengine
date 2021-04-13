package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
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

import static com.xforceplus.ultraman.oqsengine.event.EventType.*;
import static com.xforceplus.ultraman.oqsengine.event.EventType.ENTITY_DELETE;

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

    private RedisClient redisClient;

    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

//    private Thread worker;

//    private Queue<Long> expired = new LinkedBlockingQueue<>(512);

//    private static volatile boolean closed;

    //  3分钟过期
    private long expiredDuration = CacheEventHelper.EXPIRE_BUFFER_SECONDS;



    public RedisEventHandler(RedisClient redisClient, ObjectMapper objectMapper, long expiredDuration) {
        this.redisClient = redisClient;

        this.objectMapper = objectMapper;

        if (this.redisClient == null) {
            throw new IllegalArgumentException("Invalid RedisClient instance.");
        }

        if (expiredDuration > 0) {
            this.expiredDuration = expiredDuration;
        }

//        worker = new Thread(() -> {
//            while (!closed) {
//                try {
//                    if (!expired.isEmpty()) {
//                        Long txId = expired.poll();
//                        if (null != txId) {
//                            end(txId);
//                        }
//                    } else {
//                        Thread.sleep(CacheEventHelper.WAIT_DURATION);
//                    }
//
//                } catch (Exception e) {
//                    //  ignore
//
//                }
//            }
//        });
    }

    @PostConstruct
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.event");

//        closed = false;

//        worker.start();
    }

    @PreDestroy
    public void destroy() {
//        closed = true;

//        if (null != worker) {
//            waitForClosed(worker, CacheEventHelper.CLOSE_WAIT_MAX_LOOP);
//        }
    }

    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {

        if (null == id || null == version || invalidQueryEventType(eventType)) {
            //  查询对应txId的列表
            Map<String, String> result = syncCommands.hgetall(CacheEventHelper.eventKeyGenerate(txId));

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
        String rBuild = syncCommands.hget(CacheEventHelper.eventKeyGenerate(txId), CacheEventHelper.eventFieldGenerate(id, version, eventType));
        return null == rBuild || rBuild.isEmpty() ? Collections.emptyList() : Collections.singletonList(rBuild);
    }

    @Override
    public boolean create(long txId, long number, IEntity entity) {
        return storage(CacheEventHelper.generate(ENTITY_BUILD, txId, number, entity));
    }

    @Override
    public boolean replace(long txId, long number, IEntity entity, IEntity old) {
        return storage(CacheEventHelper.generate(ENTITY_BUILD, txId, number, entity, old));
    }

    @Override
    public boolean delete(long txId, long number, IEntity entity) {
        return storage(CacheEventHelper.generate(ENTITY_DELETE, txId, number, entity));
    }

    @Override
    public boolean begin(long txId) {
        return true;
    }

    @Override
    public boolean commit(long txId, long maxOpNumber) {
        end(txId);
        return true;
    }

    @Override
    public boolean rollback(long txId) {
        end(txId);
        return true;
    }



    private void end(long txId) {
        String txIdStr = CacheEventHelper.eventKeyGenerate(txId);
        try {
            if(!syncCommands.expire(txIdStr, expiredDuration)) {
                logger.warn("expired txId failed, [{}]", txIdStr);
            }
        } catch (Exception e) {
            //expired.offer(txId);
            logger.warn(e.getMessage());
        }
    }

    private boolean storage(Event<CachePayload> event) {
        try {
            String encodeJson = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(event).getBytes());

            return syncCommands.hset(CacheEventHelper.eventKeyGenerate(event.payload().get().getTxId())
                    , CacheEventHelper.eventFieldGenerate(event.payload().get().getId(),
                                        event.payload().get().getVersion(), event.type().getValue())
                    , encodeJson);
        } catch (Exception e) {
            logger.warn("storage cache-event error, [txId:{}-type:{}-id:{}-version:{}-message:{}]... "
                    , event.payload().get().getTxId(), event.type(), event.payload().get().getId(), event.payload().get().getVersion(), e.getMessage());

            return false;
        }
    }

    private boolean invalidQueryEventType(Integer eventType) {
        return null == eventType ||
                (eventType != EventType.ENTITY_BUILD.getValue() &&
                        eventType != EventType.ENTITY_REPLACE.getValue() &&
                        eventType != EventType.ENTITY_DELETE.getValue());
    }

//    private void waitForClosed(Thread thread, long maxWaitLoops) {
//        for (int i = 0; i < maxWaitLoops; i++) {
//            if (!thread.isAlive()) {
//                logger.info("wait for loops [{}] and thread closed successful.", i);
//                break;
//            }
//            sleep(CacheEventHelper.WAIT_DURATION);
//        }
//        logger.info("reach max wait loops [{}] and thread will be force-closed.", maxWaitLoops);
//    }
//
//    private void sleep(long time) {
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
