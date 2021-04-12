package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.UpdateCachePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.event.EventType.*;

/**
 * desc :
 * name : RedisEventService
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public class RedisEventService implements CacheEventService {

    static final Logger logger = LoggerFactory.getLogger(CacheEventService.class);

    private CacheEventHandler cacheEventHandler;

    public RedisEventService(CacheEventHandler cacheEventHandler) {
        this.cacheEventHandler = cacheEventHandler;
    }

    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {
        return cacheEventHandler.eventsQuery(txId, id, version, eventType);
    }

    @Override
    public boolean create(long txId, long number, IEntity entity) {
        return execute(ENTITY_BUILD, cacheEventHandler::onEventCreate, generate(ENTITY_BUILD, txId, number, entity));
    }

    @Override
    public boolean replace(long txId, long number, IEntity entity, IEntity old) {
        return execute(ENTITY_REPLACE, cacheEventHandler::onEventUpdate, generate(ENTITY_BUILD, txId, number, entity, old));
    }

    @Override
    public boolean delete(long txId, long number, IEntity entity) {
        return execute(ENTITY_DELETE, cacheEventHandler::onEventDelete, generate(ENTITY_DELETE, txId, number, entity));
    }

    @Override
    public boolean begin(long txId) {
        return cacheEventHandler.onEventBegin(txId);
    }

    @Override
    public boolean commit(long txId, long maxOpNumber) {
        return cacheEventHandler.onEventCommit(txId, maxOpNumber);
    }

    @Override
    public boolean rollback(long txId) {
        return cacheEventHandler.onEventRollback(txId);
    }

    private Event<CachePayload> generate(EventType eventType, long txId, long number, IEntity entity) {
        return new ActualEvent<>(eventType,
                        new CachePayload(txId, number, entity.id(), entity.version(), entityValues(entity)),
                        System.currentTimeMillis()
                );
    }

    private Event<UpdateCachePayload> generate(EventType eventType, long txId, long number, IEntity entity, IEntity old) {
        return new ActualEvent<>(eventType,
                new UpdateCachePayload(txId, number, entity.id(), entity.version(), entityValues(entity), entityValues(old)),
                System.currentTimeMillis()
        );
    }

    private boolean execute(EventType eventType, Function<Event, Boolean> function, Event e) {
        if (null != e && (e.payload().isPresent())) {
            return function.apply(e);
        } else {
            logger.warn("{} triggered, but event is null or payload is null..", eventType.name());
        }
        return false;
    }

    private Map<IEntityField, Object> entityValues(IEntity entity) {
        Collection<IValue> values = entity.entityValue().values();
        return values.stream().collect(Collectors.toMap(f1 -> (EntityField) f1.getField(), f1 -> ((IValue) f1.getValue()).getValue(), (f1, f2) -> f1));
    }
}
