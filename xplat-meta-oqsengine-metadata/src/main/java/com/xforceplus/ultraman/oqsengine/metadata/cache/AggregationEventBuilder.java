package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AggregationTreePayload;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.handler.EntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 聚合字段初始化字段任务.
 *
 * @className: AggregationEventBuilder
 * @date: 2021/8/31 16:24
 */
public class AggregationEventBuilder {
    final Logger logger = LoggerFactory.getLogger(AggregationEventBuilder.class);

    @Resource(name = "entityClassFormatHandler")
    private EntityClassFormatHandler entityClassFormatHandler;


    /**
     * 构建聚合事件.
     *
     * @param appId 应用id.
     * @param version 应用版本.
     * @param storageList 最新版本结构.
     * @param payLoads 事件.
     */
    public void buildAggEvent(String appId, int version,
                                 List<EntityClassStorage> storageList, List<Event<?>> payLoads) {
        if (storageList != null && storageList.size() > 0) {
            List<IEntityClass> entityClasses = this.getAggEntityClass(storageList);
            ActualEvent event = new ActualEvent<>(EventType.AGGREGATION_TREE_UPGRADE,
                    new AggregationTreePayload(appId, version, entityClasses));
            payLoads.add(event);
        }
    }

    /**
     * 将EntityClassStorage集合转成IEntityClass集合.
     *
     * @param storageList 集合.
     */
    private List<IEntityClass> getAggEntityClass(List<EntityClassStorage> storageList) {
        List<IEntityClass> entityClasses = new ArrayList<>();
        if (storageList != null && storageList.size() > 0) {
            List<EntityField> entityFields = new ArrayList<>();
            storageList.stream().map(s -> entityFields.addAll(s.getFields().stream().filter(f ->
                    f.calculationType().equals(CalculationType.AGGREGATION)
            ).collect(Collectors.toList())))
                    .collect(Collectors.toList());
            storageList.forEach(s -> {
                List<EntityField> sf = s.getFields().stream().filter(f ->
                        f.calculationType().equals(CalculationType.AGGREGATION))
                        .collect(Collectors.toList());
                if (sf.size() > 0) {
                    entityClasses.add(entityClassFormatHandler.classLoad(s.getId(), null).get());
                    sf.forEach(ef -> {
                        Aggregation aggregation = (Aggregation) ef.config().getCalculation();
                        if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                            Optional<IEntityClass> entityClassOptional = entityClassFormatHandler.classLoad(aggregation.getClassId(), null);
                            entityClasses.add(entityClassOptional.get());
                        } else {
                            Optional<IEntityClass> entityClassOptional = profileByField(aggregation.getClassId(),
                                aggregation.getFieldId(), storageList);
                            entityClasses.add(entityClassOptional.get());
                        }
                    });

                }
                Map<String, ProfileStorage> sprofiles = s.getProfileStorageMap();

                for (Map.Entry<String, ProfileStorage> entry : sprofiles.entrySet()) {
                    List<EntityField> mf = entry.getValue().getEntityFieldList().stream().filter(f ->
                            f.calculationType().equals(CalculationType.AGGREGATION))
                            .collect(Collectors.toList());
                    if (mf.size() > 0) {
                        Optional<IEntityClass> entityClassOptional = entityClassFormatHandler.classLoad(s.getId(), entry.getKey());
                        entityClasses.add(entityClassOptional.get());
                        mf.forEach(ef -> {
                            Aggregation aggregation = (Aggregation) ef.config().getCalculation();
                            Optional<IEntityClass> profileEntityClassOptional = profileByField(aggregation.getClassId(),
                                    aggregation.getFieldId(), storageList);
                            entityClasses.add(profileEntityClassOptional.get());
                        });
                    }
                }
            });
        }
        return entityClasses;
    }

    /**
     * 根据对象id和字段id找到匹配的EntityClass-支持租户.
     *
     * @param entityClassId 对象id.
     * @param fieldId 字段id.
     * @param storageList 元数据.
     */
    private Optional<IEntityClass> profileByField(long entityClassId, long fieldId, List<EntityClassStorage> storageList) {
        if (storageList != null && storageList.size() > 0) {
            List<EntityClassStorage> entityClassStorages = storageList.stream().filter(s -> s.getId() == entityClassId)
                    .collect(Collectors.toList());
            if (entityClassStorages.size() == 1) {
                List<EntityField> entityFields = entityClassStorages.get(0).getFields();
                if (entityFields.stream().filter(f -> f.id() == fieldId).count() == 0) {
                    Map<String, ProfileStorage> sprofiles = entityClassStorages.get(0).getProfileStorageMap();
                    for (Map.Entry<String, ProfileStorage> entry : sprofiles.entrySet()) {
                        List<EntityField> mf = entry.getValue().getEntityFieldList().stream().filter(f -> f.id() == fieldId)
                                .collect(Collectors.toList());
                        if (mf.size() == 1) {
                            return entityClassFormatHandler.classLoad(entityClassId, entry.getKey());
                        }
                    }
                }
                return entityClassFormatHandler.classLoad(entityClassId, null);
            }
        }
        return Optional.empty();
    }

}
