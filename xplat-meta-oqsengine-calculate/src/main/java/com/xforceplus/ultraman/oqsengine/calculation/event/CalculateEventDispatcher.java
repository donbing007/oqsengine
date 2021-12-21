package com.xforceplus.ultraman.oqsengine.calculation.event;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.CalculationEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationEventFactory;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AppMetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Logger log = LoggerFactory.getLogger(CalculateEventDispatcher .class);

    @Resource
    private EventBus eventBus;

    @Resource
    private CalculationEventFactory factory;

    @PostConstruct
    public void init() {
        eventBus.watch(EventType.META_DATA_CHANGE, event -> {
            this.dispatcher((ActualEvent<AppMetaChangePayLoad>) event);
        });
    }

    private void dispatcher(ActualEvent<AppMetaChangePayLoad> event) throws CalculationException {
        Optional<AppMetaChangePayLoad> op = event.payload();
        CachedEntityClass entityClassCacheHelper = new CachedEntityClass();
        if (op.isPresent()) {
            try {
                /**
                 * 处理FieldChanges事件
                 */
                fieldEventDispatcher(op.get(), entityClassCacheHelper);
                /**
                 * 处理RelationChanges事件
                 */
                relationEventDispatcher(op.get(), entityClassCacheHelper);
            } catch (Exception e) {
                throw new CalculationException(e.getMessage());
            }
        }
    }

    private void relationEventDispatcher(AppMetaChangePayLoad appMetaChangePayLoad, CachedEntityClass cachedEntityClass) {
        /**
         * todo 处理RelationChanges事件
         */
    }

    private void fieldEventDispatcher(AppMetaChangePayLoad appMetaChangePayLoad, CachedEntityClass cachedEntityClass)
        throws SQLException {
        Map<CalculationType, CalculationEvent> cms = toFieldChanges(appMetaChangePayLoad);

        for (Map.Entry<CalculationType, CalculationEvent> entry : cms.entrySet()) {
            CalculationEventExecutor executor = factory.executor(entry.getKey());
            if (null != executor) {
                executor.execute(entry.getValue(), cachedEntityClass, factory.resource());
            } else {
                log.warn("un-support calculate type {}, appId {}", entry.getKey(), appMetaChangePayLoad.getAppId());
            }
        }
    }

    /**
     * 按CalculationType过滤出 AppMetaChangePayLoad.EntityChange.
     * 这里会按照CalculationType分类,然后生成一个对应的事件进行执行.
     *
     * @param appMetaChangePayLoad 事件负载.
     * @return
     */
    private Map<CalculationType, CalculationEvent>
                    toFieldChanges(AppMetaChangePayLoad appMetaChangePayLoad) {
        Map<CalculationType, CalculationEvent> events = new HashMap<>();

        appMetaChangePayLoad.getEntityChanges().forEach(
            entityChange -> {
                entityChange.getFieldChanges().forEach(
                    (key, value) -> {
                        events.computeIfAbsent(key, k -> {
                            CalculationEvent event = new CalculationEvent(appMetaChangePayLoad.getAppId(), appMetaChangePayLoad.getVersion(),
                                appMetaChangePayLoad.getAppEntityClasses());

                            events.put(key, event);

                            return event;
                        }).getFieldChanges()
                            .computeIfAbsent(entityChange.getEntityClassId(), k -> new ArrayList<>())
                            .addAll(value);
                    }
                );
            }
        );

        return events;
    }
}
