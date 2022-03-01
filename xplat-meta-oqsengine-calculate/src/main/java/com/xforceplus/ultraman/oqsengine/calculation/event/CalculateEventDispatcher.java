package com.xforceplus.ultraman.oqsengine.calculation.event;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.CalculationEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationEventFactory;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.meta.MetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculateEventDispatcher {

    private Logger log = LoggerFactory.getLogger(CalculateEventDispatcher.class);

    @Resource
    private EventBus eventBus;

    @Resource
    private CalculationEventFactory factory;

    /**
     * 初始化.
     */
    @PostConstruct
    public void init() {
        eventBus.watch(EventType.META_DATA_CHANGE, event -> {
            this.dispatcher((ActualEvent<MetaChangePayLoad>) event);
        });

        log.info("init calculateEventDispatcher success.");
    }

    private void dispatcher(ActualEvent<MetaChangePayLoad> event) throws CalculationException {
        Optional<MetaChangePayLoad> op = event.payload();
        CachedEntityClass cachedEntityClass = new CachedEntityClass();
        if (op.isPresent()) {
            try {
                //  处理FieldChanges事件.
                fieldEventDispatcher(op.get(), cachedEntityClass);

                //  处理RelationChanges事件.
                relationEventDispatcher(op.get(), cachedEntityClass);
            } catch (Exception e) {
                throw new CalculationException(e.getMessage());
            }
        }
    }

    /**
     * 处理relation的事件变更.
     */
    private void relationEventDispatcher(MetaChangePayLoad appMetaChangePayLoad, CachedEntityClass cachedEntityClass) {
        //  todo 处理RelationChanges事件
    }

    /**
     * 处理field的事件变更.
     */
    private void fieldEventDispatcher(MetaChangePayLoad metaChangePayLoad, CachedEntityClass cachedEntityClass)
        throws SQLException {
        Map<CalculationType, CalculationEvent> events = new HashMap<>();
        Map<Long, List<Long>> deletes = new HashMap<>();
        metaChangePayLoad.getEntityChanges().forEach(
            entityChange -> {
                entityChange.getFieldChanges().forEach(
                    fieldChange -> {
                        switch (fieldChange.getOp()) {
                            case CREATE:
                            case UPDATE:
                                IEntityClass entityClass = cachedEntityClass.findEntityClassWithCache(
                                    factory.resource().getMetaManager(), entityChange.getEntityClassId(), fieldChange.getProfile(), metaChangePayLoad.getVersion()
                                );
                                if (null != entityClass) {
                                    entityClass.field(fieldChange.getFieldId()).ifPresent(
                                        entityField -> addCalculationField(events, metaChangePayLoad, entityChange, fieldChange, entityField));
                                }
                                break;
                            case DELETE:
                                //  加入到删除列表.
                                deletes.computeIfAbsent(entityChange.getEntityClassId(), k -> new ArrayList<>()).add(fieldChange.getFieldId());
                                break;
                            default:
                                break;
                        }
                    }
                );
            }
        );

        for (Map.Entry<CalculationType, CalculationEvent> entry : events.entrySet()) {
            CalculationEventExecutor executor = factory.executor(entry.getKey());
            if (null != executor) {
                executor.execute(entry.getValue(), cachedEntityClass, factory.resource());
            } else {
                log.warn("un-support calculate type {}, appId {}", entry.getKey(), metaChangePayLoad.getAppId());
            }
        }

        //  todo 对deletes的操作.
        if (!deletes.isEmpty()) {
            factory.executor(CalculationType.STATIC).deleteTypeExecute(deletes, factory.resource());
        }
    }

    private void addCalculationField(Map<CalculationType, CalculationEvent> events, MetaChangePayLoad metaChangePayLoad,
                                     MetaChangePayLoad.EntityChange entityChange, MetaChangePayLoad.FieldChange fieldChange,
                                     IEntityField entityField) {
        events
            .computeIfAbsent(entityField.calculationType(), k -> {
                return new CalculationEvent(metaChangePayLoad.getAppId(),
                    metaChangePayLoad.getVersion());
            })
            .getCalculationFields()
            .computeIfAbsent(entityChange.getEntityClassId(), f -> new ArrayList<>())
            .add(new CalculationEvent.CalculationField(fieldChange.getOp(),
                fieldChange.getProfile(), entityField));
    }
}
