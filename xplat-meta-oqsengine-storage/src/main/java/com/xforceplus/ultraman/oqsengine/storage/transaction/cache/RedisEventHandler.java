package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import static com.xforceplus.ultraman.oqsengine.event.EventType.ENTITY_BUILD;
import static com.xforceplus.ultraman.oqsengine.event.EventType.ENTITY_DELETE;
import static com.xforceplus.ultraman.oqsengine.event.EventType.ENTITY_REPLACE;
import static com.xforceplus.ultraman.oqsengine.event.EventType.TX_BEGIN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.gzip.ZipUtils;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : RedisEventHandler
 *
 * @author : xujia 2021/4/8
 * @since : 1.8
 */
public class RedisEventHandler implements CacheEventHandler, Lifecycle {

    final Logger logger = LoggerFactory.getLogger(RedisEventHandler.class);

    private RedisClient redisClient;

    private ObjectMapper objectMapper;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    //  3分钟过期
    private long expiredDuration = CacheEventHelper.EXPIRE_BUFFER_SECONDS;

    /**
     * 实例化.
     */
    public RedisEventHandler(RedisClient redisClient, ObjectMapper objectMapper, long expiredDuration) {
        this.redisClient = redisClient;

        this.objectMapper = objectMapper;

        if (this.redisClient == null) {
            throw new IllegalArgumentException("Invalid RedisClient instance.");
        }

        if (expiredDuration > 0) {
            this.expiredDuration = expiredDuration;
        }

    }

    @PostConstruct
    @Override
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.event");
    }

    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {

        if (null == id || null == version || invalidQueryEventType(eventType)) {
            //  查询对应txId的列表
            Map<String, String> result = syncCommands.hgetall(CacheEventHelper.eventKeyGenerate(txId));

            return null == result || result.isEmpty()
                ? Collections.emptyList() : result.entrySet().stream()
                .filter(e -> !e.getKey().equals(TX_BEGIN.name())).map(Map.Entry::getValue).collect(Collectors.toList());
        }

        //  精确查询 txId + id + version 确定
        String rightBuild = syncCommands
            .hget(CacheEventHelper.eventKeyGenerate(txId), CacheEventHelper.eventFieldGenerate(id, version, eventType));
        return null == rightBuild || rightBuild.isEmpty() ? Collections.emptyList() :
            Collections.singletonList(rightBuild);
    }

    @Override
    public boolean create(long txId, long number, IEntity entity) {
        return storage(CacheEventHelper.toCachePayload(ENTITY_BUILD, txId, number, entity, null));
    }

    @Override
    public boolean replace(long txId, long number, IEntity entity, IEntity old) {
        return storage(CacheEventHelper.toCachePayload(ENTITY_REPLACE, txId, number, entity, old));
    }

    @Override
    public boolean delete(long txId, long number, IEntity entity) {
        return storage(CacheEventHelper.toCachePayload(ENTITY_DELETE, txId, number, entity, null));
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
            if (!syncCommands.expire(txIdStr, expiredDuration)) {
                logger.warn("expired cache event item failed, txId-[{}]", txIdStr);
            }
        } catch (Exception e) {
            //expired.offer(txId);
            logger.warn(e.getMessage());
        }
    }

    private boolean storage(CachePayload payload) {
        try {
            String encodeJson = ZipUtils.zip(objectMapper.writeValueAsString(payload));

            return syncCommands.hset(CacheEventHelper.eventKeyGenerate(payload.getTxId()),
                CacheEventHelper.eventFieldGenerate(payload.getId(), payload.getVersion(), payload.getEventType().getValue()),
                encodeJson);
        } catch (Exception e) {
            logger.warn("storage cache-event error, [txId:{}-type:{}-id:{}-version:{}-message:{}]... ",
                payload.getTxId(), payload.getEventType(), payload.getId(), payload.getVersion(), e.toString());

            return false;
        }
    }

    private boolean invalidQueryEventType(Integer eventType) {
        return null == eventType
            || (eventType != EventType.ENTITY_BUILD.getValue()
            && eventType != EventType.ENTITY_REPLACE.getValue()
            && eventType != EventType.ENTITY_DELETE.getValue());
    }
}
