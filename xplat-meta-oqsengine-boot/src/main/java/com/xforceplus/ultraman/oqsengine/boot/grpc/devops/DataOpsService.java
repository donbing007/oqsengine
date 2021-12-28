package com.xforceplus.ultraman.oqsengine.boot.grpc.devops;

import com.xforceplus.ultraman.devops.service.sdk.annotation.DiscoverAction;
import com.xforceplus.ultraman.devops.service.sdk.annotation.MethodParam;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.PrintErrorHelper;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsDataResponse;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQueryConfig;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQueryResponse;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQuerySummary;
import com.xforceplus.ultraman.oqsengine.devops.om.util.DevOpsOmDataUtils;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
@Component
public class DataOpsService {

    @Autowired
    private MetaManager metaManager;

    @Autowired
    private EntityManagementService entityManagementService;

    @Autowired
    private EntitySearchService entitySearchService;


    /**
     * 统一数据运维-条件查询.
     *
     * @param config 请求配置
     * @return 返回结果
     */
    @DiscoverAction(describe = "条件查询", retClass = Collection.class)
    public DevOpsQueryResponse conditionQuery(
            @MethodParam(name = "config", klass = DevOpsQueryConfig.class) DevOpsQueryConfig config
    ) {
        try {
            Optional<IEntityClass> entityClassOptl = metaManager.load(config.getEntityClassId(), null);
            if (!entityClassOptl.isPresent()) {
                return null;
            }

            ServiceSelectConfig.Builder serviceSelectConfigBuilder = ServiceSelectConfig.Builder.anSearchConfig()
                    .withPage(new Page(config.getPageNo(), config.getPageSize()));

            Facet facet = Facet.build();
            entityClassOptl.get().fields().stream()
                    .map(field -> facet.getFields().add(field));
            serviceSelectConfigBuilder.withFacet(facet);

            if (config.getSort() != null && config.getSort().size() > 0) {
                List<Sort> sorts = new ArrayList<>();
                config.getSort().forEach(s -> {
                    Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                            .stream().filter(field -> s.getField().equals(field.name())).findAny();
                    if (entityFieldOptl.isPresent()) {
                        if ("asc".equals(s.getOrder())) {
                            sorts.add(Sort.buildAscSort(entityFieldOptl.get()));
                        } else if ("desc".equals(s.getOrder())) {
                            sorts.add(Sort.buildDescSort(entityFieldOptl.get()));
                        }
                    }
                });
                if (sorts.size() > 0) {
                    serviceSelectConfigBuilder.withSort(sorts.get(0));
                }
                if (sorts.size() > 1) {
                    serviceSelectConfigBuilder.withSecondarySort(sorts.get(1));
                }
                if (sorts.size() > 2) {
                    serviceSelectConfigBuilder.withThridSort(sorts.get(2));
                }
            }
            ServiceSelectConfig serviceSelectConfig = serviceSelectConfigBuilder.build();

            Conditions conditions = Conditions.buildEmtpyConditions();
            config.getConditions().getFields().stream().forEach(c -> {
                Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                        .stream().filter(field -> c.getCode().equals(field.name())).findAny();
                if (entityFieldOptl.isPresent()) {
                    ConditionOperator operation = DevOpsOmDataUtils.convertOperation(c.getOperation());
                    if (operation == null) {
                        if (!StringUtils.isEmpty(c.getOperation())) {
                            String[] operations = c.getOperation().split("_");
                            if (operations.length == 2) {
                                ConditionOperator operation0 = DevOpsOmDataUtils.convertOperation(operations[0]);
                                ConditionOperator operation1 = DevOpsOmDataUtils.convertOperation(operations[1]);
                                if (operation0 != null && operation1 != null && c.getValue() != null && c.getValue().length == 2) {
                                    IValue minValue = IValueUtils.toIValue(entityFieldOptl.get(), DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), c.getValue()[0]));
                                    conditions.addAnd(new Condition(
                                            entityFieldOptl.get(),
                                            operation0,
                                            new IValue[]{minValue}
                                    ));
                                    IValue maxValue = IValueUtils.toIValue(entityFieldOptl.get(), DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), c.getValue()[1]));
                                    conditions.addAnd(new Condition(
                                            entityFieldOptl.get(),
                                            operation1,
                                            new IValue[]{maxValue}
                                    ));
                                }
                            }
                        }
                    } else {
                        List<IValue> values =
                                Arrays.asList(c.getValue())
                                        .stream().map(v -> IValueUtils.toIValue(entityFieldOptl.get(), DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), v)))
                                        .collect(Collectors.toList());
                        Condition condition = new Condition(
                                entityFieldOptl.get(),
                                operation,
                                values.toArray(new IValue[]{})
                        );
                        conditions.addAnd(condition);
                    }
                }
            });

            Collection<IEntity> entities = entitySearchService.selectByConditions(conditions, new EntityClassRef(config.getEntityClassId(), ""), serviceSelectConfig);

            DevOpsQueryResponse response = new DevOpsQueryResponse();
            response.setRows(entities.stream().map(entity -> {
                Map map = new HashMap();
                map.put("id", String.valueOf(entity.id()));
                entity.entityValue().values().forEach(value -> {
                    map.put(value.getField().name(), value.valueToString());
                });
                return map;
            }).collect(Collectors.toList()));
            DevOpsQuerySummary summary = new DevOpsQuerySummary();
            if (serviceSelectConfig.getPage().isPresent()) {
                summary.setTotal(serviceSelectConfig.getPage().get().getTotalCount());
            } else {
                summary.setTotal(0L);
            }
            response.setSummary(summary);
            return response;
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle(String.format("selectByConditions exception, [%s]", config.getEntityClassId()), e);
        }
        return null;
    }

    /**
     * 统一数据运维-新增.
     *
     * @param entityClassId 实体ID
     * @param data 请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "新增", retClass = Collection.class)
    public DevOpsDataResponse singleCreate(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "data", klass = Map.class) Map data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return null;
        }
        EntityClassRef entityClassRef = EntityClassRef
                .Builder
                .anEntityClassRef()
                .withEntityClassId(entityClassId)
                .withEntityClassCode(entityClassOptl.get().code())
                .build();
        IEntityValue entityValue = EntityValue.build();
        entityClassOptl.get().fields().stream().forEach(field -> {
            if ("create_time".equals(field.name())) {
                entityValue.addValue(new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
            }
        });
        data.keySet().stream().forEach(fieldCode -> {
            Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                    .stream().filter(field -> fieldCode.equals(field.name())).findAny();
            if (entityFieldOptl.isPresent()) {
                Optional<IValue> valueOptl = entityFieldOptl.get().type()
                        .toTypedValue(entityFieldOptl.get(), String.valueOf(data.get(fieldCode)));
                if (valueOptl.isPresent()) {
                    entityValue.addValue(valueOptl.get());
                } else {
                    entityValue.addValue(new EmptyTypedValue(entityFieldOptl.get()));
                }
            }
        });
        IEntity targetEntity = Entity.Builder.anEntity()
                .withEntityClassRef(entityClassRef)
                .withTime(System.currentTimeMillis())
                .withEntityValue(entityValue).build();

        try {
            return toDevOpsDataResponse(
                    entityManagementService.build(targetEntity)
            );
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(String.format("devops om singleCreate exception, [%s]", entityClassId), e);
        }

        return null;
    }

    /**
     * 统一数据运维-修改.
     *
     * @param entityClassId 实体ID
     * @param entityValueId 实体数据ID
     * @param data 请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "修改", retClass = Collection.class)
    public DevOpsDataResponse singleModify(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "entityValueId", klass = long.class, required = true) long entityValueId,
            @MethodParam(name = "data", klass = Map.class) Map data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return null;
        }
        EntityClassRef entityClassRef = EntityClassRef
                .Builder
                .anEntityClassRef()
                .withEntityClassId(entityClassId)
                .withEntityClassCode(entityClassOptl.get().code())
                .build();
        IEntityValue entityValue = EntityValue.build();
        entityClassOptl.get().fields().stream().forEach(field -> {
            if ("update_time".equals(field.name())) {
                entityValue.addValue(new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
            }
        });
        data.keySet().forEach(fieldCode -> {
            Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                    .stream().filter(field -> fieldCode.equals(field.name())).findAny();
            if (entityFieldOptl.isPresent()) {
                IValue value = IValueUtils.toIValue(entityFieldOptl.get(), DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), data.get(fieldCode)));
                entityValue.addValue(value);
            }
        });
        IEntity targetEntity = Entity.Builder.anEntity()
                .withEntityClassRef(entityClassRef)
                .withId(entityValueId)
                .withTime(System.currentTimeMillis())
                .withEntityValue(entityValue).build();

        try {
            return toDevOpsDataResponse(
                    entityManagementService.replace(targetEntity)
            );
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(String.format("devops om singleModify exception, [%s-%s]", entityClassId, entityValueId), e);
        }

        return null;
    }

    /**
     * 统一数据运维-删除.
     *
     * @param entityClassId 实体ID
     * @param entityValueId 实体数据ID
     * @return 返回结果
     */
    @DiscoverAction(describe = "删除", retClass = Collection.class)
    public DevOpsDataResponse singleDelete(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "entityValueId", klass = long.class, required = true) long entityValueId) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return null;
        }

        EntityClassRef entityClassRef = EntityClassRef
                .Builder
                .anEntityClassRef()
                .withEntityClassId(entityClassId)
                .withEntityClassCode(entityClassOptl.get().code())
                .build();

        IEntity targetEntity = Entity.Builder.anEntity()
                .withEntityClassRef(entityClassRef)
                .withId(entityValueId)
                .withTime(System.currentTimeMillis())
                .build();

        try {
            return toDevOpsDataResponse(
                    entityManagementService.build(targetEntity)
            );
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(String.format("devops om singleDelete exception, [%s-%s]", entityClassId, entityValueId), e);
        }

        return null;
    }

    private DevOpsDataResponse toDevOpsDataResponse(OperationResult operationResult) {
        return new DevOpsDataResponse(
                operationResult.getTxId(),
                operationResult.getEntityId(),
                operationResult.getVersion(),
                operationResult.getEventType(),
                operationResult.getMessage());
    }

}
