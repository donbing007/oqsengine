package com.xforceplus.ultraman.oqsengine.boot.grpc.devops;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.devops.service.sdk.annotation.DiscoverAction;
import com.xforceplus.ultraman.devops.service.sdk.annotation.MethodParam;
import com.xforceplus.ultraman.devops.service.sdk.config.context.AuthContext;
import com.xforceplus.ultraman.devops.service.transfer.generate.Auth;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.PrintErrorHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.InitCalculationManager;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.facet.Facet;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
@Component
public class DataOpsService {

    private final Logger logger = LoggerFactory.getLogger(DataOpsService.class);

    @Autowired
    private MetaManager metaManager;

    @Autowired
    private EntityManagementService entityManagementService;

    @Autowired
    private EntitySearchService entitySearchService;

    @Autowired
    private InitCalculationManager initCalculationManager;


    private final int batchLimit = 1000;

    private final String operateSysBo = "oqsengineSdkOmAuditLog";

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
                                if (operation0 != null && operation1 != null && c.getValue() != null
                                    && c.getValue().length == 2) {
                                    IValue minValue = IValueUtils.toIValue(entityFieldOptl.get(),
                                        DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), c.getValue()[0]));
                                    conditions.addAnd(new Condition(
                                        entityFieldOptl.get(),
                                        operation0,
                                        new IValue[] {minValue}
                                    ));
                                    IValue maxValue = IValueUtils.toIValue(entityFieldOptl.get(),
                                        DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), c.getValue()[1]));
                                    conditions.addAnd(new Condition(
                                        entityFieldOptl.get(),
                                        operation1,
                                        new IValue[] {maxValue}
                                    ));
                                }
                            }
                        }
                    } else {
                        if (operation.equals(ConditionOperator.IS_NOT_NULL) || operation.equals(ConditionOperator.IS_NULL)) {
                            Condition condition = new Condition(
                                    entityFieldOptl.get(),
                                    operation,
                                    new EmptyTypedValue(entityFieldOptl.get())
                            );
                            conditions.addAnd(condition);
                        } else {
                            List<IValue> values =
                                    Arrays.asList(c.getValue())
                                            .stream().map(v -> IValueUtils.toIValue(entityFieldOptl.get(),
                                                    DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), v)))
                                            .collect(Collectors.toList());
                            Condition condition = new Condition(
                                    entityFieldOptl.get(),
                                    operation,
                                    values.toArray(new IValue[]{})
                            );
                            conditions.addAnd(condition);
                        }
                    }
                }
            });

            OqsResult<Collection<IEntity>> entities =
                entitySearchService.selectByConditions(conditions, new EntityClassRef(config.getEntityClassId(), ""),
                    serviceSelectConfig);

            DevOpsQueryResponse response = new DevOpsQueryResponse();
            response.setRows(entities.getValue().get().stream().map(entity -> {
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
            PrintErrorHelper.exceptionHandle(
                String.format("selectByConditions exception, [%s]", config.getEntityClassId()), e);
        }
        return null;
    }

    /**
     * 统一数据运维-新增.
     *
     * @param entityClassId 实体ID
     * @param data          请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "新增", retClass = Collection.class)
    public DevOpsDataResponse singleCreate(
        @MethodParam(name = "appId", klass = long.class, required = true) long appId,
        @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
        @MethodParam(name = "data", klass = Map.class) Map data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        List<IValue> entityValue = new ArrayList<>();

        entityValue.addAll(buildCreateInfoField(entityClassOptl.get()));

        data.keySet().stream().forEach(fieldCode -> {
            Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                .stream().filter(field -> fieldCode.equals(field.name())).findAny();
            if (entityFieldOptl.isPresent()) {
                Optional<IValue> valueOptl = entityFieldOptl.get().type()
                    .toTypedValue(entityFieldOptl.get(), String.valueOf(data.get(fieldCode)));
                if (valueOptl.isPresent()) {
                    entityValue.add(valueOptl.get());
                } else {
                    entityValue.add(new EmptyTypedValue(entityFieldOptl.get()));
                }
            }
        });
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(entityClassOptl.get().ref())
            .withTime(System.currentTimeMillis())
            .withValues(entityValue).build();

        OqsResult<IEntity> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.build(targetEntity);

            saveOperate(appId, entityClassId, null, OperateType.SINGLE_CREATE, data, oqsResult);
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(String.format("devops om singleCreate exception, [%s]", entityClassId), e);
        }

        return toDevOpsDataResponse(oqsResult);
    }

    /**
     * 统一数据运维-修改.
     *
     * @param entityClassId 实体ID
     * @param entityValueId 实体数据ID
     * @param data          请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "修改", retClass = Collection.class)
    public DevOpsDataResponse singleModify(
        @MethodParam(name = "appId", klass = long.class, required = true) long appId,
        @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
        @MethodParam(name = "entityValueId", klass = long.class, required = true) long entityValueId,
        @MethodParam(name = "data", klass = Map.class) Map data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        List<IValue> entityValue = new ArrayList<>();

        entityValue.addAll(buildUpdateInfoField(entityClassOptl.get()));

        data.keySet().forEach(fieldCode -> {
            Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                .stream().filter(field -> fieldCode.equals(field.name())).findAny();
            if (entityFieldOptl.isPresent()) {
                IValue value = IValueUtils.toIValue(entityFieldOptl.get(),
                    DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), data.get(fieldCode)));
                entityValue.add(value);
            }
        });
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(entityClassOptl.get().ref())
            .withId(entityValueId)
            .withTime(System.currentTimeMillis())
            .withValues(entityValue).build();

        OqsResult<Map.Entry<IEntity, IValue[]>> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.replace(targetEntity);

            saveOperate(appId, entityClassId, entityValueId, OperateType.SINGLE_MODIFY, data, oqsResult);
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(
                String.format("devops om singleModify exception, [%s-%s]", entityClassId, entityValueId), e);
        }

        return toDevOpsDataResponse(oqsResult);
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
        @MethodParam(name = "appId", klass = long.class, required = true) long appId,
        @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
        @MethodParam(name = "entityValueId", klass = long.class, required = true) long entityValueId) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(entityClassOptl.get().ref())
            .withId(entityValueId)
            .withTime(System.currentTimeMillis())
            .build();

        OqsResult<IEntity> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.delete(targetEntity);

            saveOperate(appId, entityClassId, entityValueId, OperateType.SINGLE_DELETE, null, oqsResult);
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(
                String.format("devops om singleDelete exception, [%s-%s]", entityClassId, entityValueId), e);
        }

        return toDevOpsDataResponse(oqsResult);
    }

    /**
     * 统一数据运维-批量新增.
     *
     * @param entityClassId 实体ID
     * @param data          请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "批量新增", retClass = Collection.class)
    public DevOpsDataResponse batchCreate(
            @MethodParam(name = "appId", klass = long.class, required = true) long appId,
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "data", klass = Map.class) List<Map> data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        if (data == null || data.size() == 0) {
            return DevOpsDataResponse.fail("没有可新增的数据");
        }

        if (data.size() > batchLimit) {
            return new DevOpsDataResponse(String.format("批量新增的数据超过上限（%d）", batchLimit));
        }

        boolean idExist = false;
        for (Map item : data) {
            if (item.containsKey("id")) {
                idExist = true;
            }
        }
        if (idExist) {
            return DevOpsDataResponse.fail("新增的批量数据不能存在Id值");
        }

        List<IEntity> entityList = data.stream().map(map -> {
            List<IValue> entityValue = new ArrayList<>();

            entityValue.addAll(buildCreateInfoField(entityClassOptl.get()));

            map.keySet().forEach(fieldCode -> {
                Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                        .stream().filter(field -> fieldCode.equals(field.name())).findAny();
                if (entityFieldOptl.isPresent()) {
                    IValue value = IValueUtils.toIValue(entityFieldOptl.get(),
                            DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), map.get(fieldCode)));
                    entityValue.add(value);
                }
            });

            return Entity.Builder.anEntity()
                    .withEntityClassRef(entityClassOptl.get().ref())
                    .withTime(System.currentTimeMillis())
                    .withValues(entityValue).build();
        }).collect(Collectors.toList());

        OqsResult<IEntity[]> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.build(entityList.toArray(new IEntity[]{}));

            saveOperate(appId, entityClassId, null, OperateType.BATCH_CREATE, data, oqsResult);
        } catch (SQLException e) {
            String idStrs = data.stream().map(item -> String.valueOf(item.get("id"))).collect(Collectors.joining(","));
            PrintErrorHelper.exceptionHandle(
                    String.format("devops om batchCreate exception, [%s], %s", entityClassId, idStrs), e);
        }

        return toDevOpsDataResponse(oqsResult);
    }

    /**
     * 统一数据运维-批量修改.
     *
     * @param entityClassId 实体ID
     * @param data          请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "批量修改", retClass = Collection.class)
    public DevOpsDataResponse batchModify(
            @MethodParam(name = "appId", klass = long.class, required = true) long appId,
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "data", klass = Map.class) List<Map> data) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        if (data == null || data.size() == 0) {
            return DevOpsDataResponse.fail("没有可修改的数据");
        }

        if (data.size() > batchLimit) {
            return new DevOpsDataResponse(String.format("批量修改的数据超过上限（%d）", batchLimit));
        }

        boolean illegalIdExist = false;
        for (Map item : data) {
            if (item.containsKey("id")) {
                String id = (String) item.get("id");
                if (!StringUtils.isNumeric(id)) {
                    illegalIdExist = true;
                }
            } else {
                illegalIdExist = true;
            }
        }
        if (illegalIdExist) {
            return DevOpsDataResponse.fail("数据存在不合法的Id");
        }

        List<IEntity> entityList = data.stream().map(map -> {
            List<IValue> entityValue = new ArrayList<>();

            entityValue.addAll(buildUpdateInfoField(entityClassOptl.get()));

            map.keySet().stream().filter(fieldCode -> !"id".equals(fieldCode)).forEach(fieldCode -> {
                Optional<IEntityField> entityFieldOptl = entityClassOptl.get().fields()
                        .stream().filter(field -> fieldCode.equals(field.name())).findAny();
                if (entityFieldOptl.isPresent()) {
                    IValue value = IValueUtils.toIValue(entityFieldOptl.get(),
                            DevOpsOmDataUtils.convertDataObject(entityFieldOptl.get(), map.get(fieldCode)));
                    entityValue.add(value);
                }
            });

            long entityValueId = Long.valueOf((String) map.get("id"));
            return Entity.Builder.anEntity()
                    .withEntityClassRef(entityClassOptl.get().ref())
                    .withId(entityValueId)
                    .withTime(System.currentTimeMillis())
                    .withValues(entityValue).build();
        }).collect(Collectors.toList());

        OqsResult<Map<IEntity, IValue[]>> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.replace(entityList.toArray(new IEntity[]{}));

            saveOperate(appId, entityClassId, null, OperateType.BATCH_MODIFY, data, oqsResult);
        } catch (SQLException e) {
            String idStrs = data.stream().map(item -> String.valueOf(item.get("id"))).collect(Collectors.joining(","));
            PrintErrorHelper.exceptionHandle(
                    String.format("devops om batchModify exception, [%s], %s", entityClassId, idStrs), e);
        }

        return toDevOpsDataResponse(oqsResult);
    }

    /**
     * 统一数据运维-批量删除.
     *
     * @param entityClassId 实体ID
     * @param idStrs          请求参数
     * @return 返回结果
     */
    @DiscoverAction(describe = "批量删除", retClass = Collection.class)
    public DevOpsDataResponse batchDelete(
            @MethodParam(name = "appId", klass = long.class, required = true) long appId,
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "data", klass = Map.class) List<String> idStrs) {
        Optional<IEntityClass> entityClassOptl = metaManager.load(entityClassId, null);
        if (!entityClassOptl.isPresent()) {
            return DevOpsDataResponse.fail(String.format("根据对象ID（%d）找不到对象元数据信息", entityClassId));
        }

        if (idStrs == null || idStrs.size() == 0) {
            return DevOpsDataResponse.fail("没有可修改的数据");
        }

        if (idStrs.size() > batchLimit) {
            return DevOpsDataResponse.fail(String.format("批量删除的数据超过上限（%d）", batchLimit));
        }

        boolean illegalIdExist = false;
        for (String idStr : idStrs) {
            if (!StringUtils.isNumeric(idStr)) {
                illegalIdExist = true;
            }
        }
        if (illegalIdExist) {
            return DevOpsDataResponse.fail("数据存在不合法的ID");
        }

        List<IEntity> entityList = idStrs.stream().map(idStr ->
            Entity.Builder.anEntity()
                    .withEntityClassRef(entityClassOptl.get().ref())
                    .withId(Long.valueOf(idStr))
                    .withTime(System.currentTimeMillis())
                    .build()
        ).collect(Collectors.toList());

        OqsResult<IEntity[]> oqsResult = OqsResult.unknown();
        try {
            oqsResult = entityManagementService.delete(entityList.toArray(new IEntity[]{}));

            saveOperate(appId, entityClassId, null, OperateType.BATCH_DELETE, idStrs, oqsResult);
        } catch (SQLException e) {
            PrintErrorHelper.exceptionHandle(
                    String.format("devops om batchDelete exception, [%s], %s", entityClassId, JSON.toJSONString(idStrs)), e);
        }

        return toDevOpsDataResponse(oqsResult);
    }

    /**
     * 统一数据运维-初始化计算字段.
     *
     * @Param appId 应用Id.
     * @return 返回结果
     */
    @DiscoverAction(describe = "初始化计算字段", retClass = List.class)
    public List<IEntityField> initAppCalculations(@MethodParam(name = "appId", klass = long.class, required = true) long appId) {
        return initCalculationManager.initAppCalculations(Long.valueOf(appId).toString());
    }

    private DevOpsDataResponse toDevOpsDataResponse(OqsResult oqsResult) {
        return DevOpsDataResponse.ok(oqsResult.getMessage());
    }

    private void saveOperate(long appId, long entityClassId, Long entityValueId, OperateType operateType, Object reqData, Object respData) {
        try {
            Collection<IEntityClass> entityClasses = metaManager.appLoad(Long.valueOf(appId).toString());
            if (entityClasses.isEmpty()) {
                logger.warn("应用[{}]找不到对象列表", appId);
                return;
            }
            Optional<IEntityClass> entityClassOptl = entityClasses.stream()
                    .filter(entityClass -> operateSysBo.equals(entityClass.code())).findAny();
            if (!entityClassOptl.isPresent()) {
                logger.warn("应用[{}]找不到记录操作日志对象[{}]", appId, entityClassOptl.get().id());
                return;
            }
            Auth auth = AuthContext.get();
            List<IValue> entityValue = new ArrayList<>();
            entityClassOptl.get().fields().stream().forEach(field -> {
                if ("create_time".equals(field.name())) {
                    entityValue.add(
                            new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
                } else if ("app_id".equals(field.name())) {
                    entityValue.add(
                            new LongValue(field, appId));
                } else if ("bo_id".equals(field.name())) {
                    entityValue.add(
                            new LongValue(field, entityClassId));
                } else if ("entity_id".equals(field.name())) {
                    if (entityValueId != null) {
                        entityValue.add(
                                new LongValue(field, entityValueId));
                    }
                } else if ("request_data".equals(field.name())) {
                    if (reqData != null) {
                        entityValue.add(
                                new StringValue(field, JSON.toJSONString(reqData)));
                    }
                } else if ("response_data".equals(field.name())) {
                    entityValue.add(
                            new StringValue(field, JSON.toJSONString(convertOqsResult(operateType, respData))));
                } else if ("operator_id".equals(field.name())) {
                    if (auth != null && !StringUtils.isEmpty(auth.getId())) {
                        entityValue.add(
                                new LongValue(field, Long.valueOf(auth.getId())));
                    }
                } else if ("operator_code".equals(field.name())) {
                    if (auth != null && !StringUtils.isEmpty(auth.getLoginName())) {
                        entityValue.add(
                                new StringValue(field, auth.getLoginName()));
                    }
                } else if ("operator_name".equals(field.name())) {
                    if (auth != null && !StringUtils.isEmpty(auth.getUsername())) {
                        entityValue.add(
                                new StringValue(field, auth.getUsername()));
                    }
                } else if ("operate_type".equals(field.name())) {
                    entityValue.add(
                            new StringValue(field, operateType.name()));
                } else if ("operate_time".equals(field.name())) {
                    entityValue.add(
                            new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
                }
            });

            IEntity targetEntity = Entity.Builder.anEntity()
                    .withEntityClassRef(entityClassOptl.get().ref())
                    .withTime(System.currentTimeMillis())
                    .withValues(entityValue)
                    .build();
            try {
                entityManagementService.build(targetEntity);
            } catch (SQLException e) {
                logger.error("应用[{}]记录操作日志对象[{}]保存数据失败", appId, entityClassOptl.get().id(), e);
            }
        } catch (Exception e) {
            logger.error("save operation info fail", e);
        }
    }

    private Map convertOqsResult(OperateType operateType, Object respData) {
        Map respMap = new HashMap();
        if (OperateType.SINGLE_CREATE.equals(operateType)
                || OperateType.SINGLE_DELETE.equals(operateType)) {
            OqsResult<IEntity> oqsResult = (OqsResult<IEntity>) respData;
            respMap.put("status", oqsResult.getResultStatus().name());
            if (oqsResult.getValue().isPresent()) {
                respMap.put("data", oqsResult.getValue().get().entityValue().values().stream().collect(Collectors.toMap(v -> v.getField().name(), v -> v.getValue())));
            }
        } else if (OperateType.SINGLE_MODIFY.equals(operateType)) {
            OqsResult<Map.Entry<IEntity, IValue[]>> oqsResult = (OqsResult<Map.Entry<IEntity, IValue[]>>) respData;
            respMap.put("status", oqsResult.getResultStatus().name());
            if (oqsResult.getValue().isPresent()) {
                respMap.put("data", Arrays.stream(oqsResult.getValue().get().getValue()).collect(Collectors.toMap(v -> v.getField().name(), v -> v.getValue())));
            }
        } else if (OperateType.BATCH_CREATE.equals(operateType)
                || OperateType.BATCH_DELETE.equals(operateType)) {
            OqsResult<IEntity[]> oqsResult = (OqsResult<IEntity[]>) respData;
            respMap.put("status", oqsResult.getResultStatus().name());
            if (oqsResult.getValue().isPresent()) {
                IEntity[] data = oqsResult.getValue().get();
                Map dataMap = new HashMap();
                Arrays.stream(data).map(entity ->
                    dataMap.put(entity.id(), entity.entityValue().values().stream().collect(Collectors.toMap(v -> v.getField().name(), v -> v.getValue())))
                );
                respMap.put("data", Arrays.stream(data).map(item -> item.entityValue().values()).collect(Collectors.toList()));
            }
        } else if (OperateType.BATCH_MODIFY.equals(operateType)) {
            OqsResult<Map<IEntity, IValue[]>> oqsResult = (OqsResult<Map<IEntity, IValue[]>>) respData;
            respMap.put("status", oqsResult.getResultStatus().name());
            if (oqsResult.getValue().isPresent()) {
                Map<IEntity, IValue[]> data = oqsResult.getValue().get();
                Map dataMap = new HashMap();
                data.keySet().forEach(key ->
                    dataMap.put(key.id(), Arrays.stream(data.get(key)).collect(Collectors.toMap(v -> v.getField().name(), v -> v.getValue())))
                );
                respMap.put("data", dataMap);
            }
        }

        return respMap;
    }

    private List<IValue> buildCreateInfoField(IEntityClass entityClass) {
        List<IValue> entityValue = new ArrayList<>();
        if (entityClass == null) {
            return entityValue;
        }
        Auth auth = AuthContext.get();
        entityClass.fields().stream().forEach(field -> {
            switch (field.name()) {
                case "create_time":
                    entityValue.add(
                            new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
                    break;
                case "create_user_id":
                    if (auth != null && !StringUtils.isEmpty(auth.getId())) {
                        entityValue.add(
                                new LongValue(field, Long.valueOf(auth.getId())));
                    }
                    break;
                case "create_user_name":
                    if (auth != null && !StringUtils.isEmpty(auth.getUsername())) {
                        entityValue.add(
                                new StringValue(field, auth.getUsername()));
                    }
                    break;
                default:
            }
        });
        return entityValue;
    }

    private List<IValue> buildUpdateInfoField(IEntityClass entityClass) {
        List<IValue> entityValue = new ArrayList<>();
        if (entityClass == null) {
            return entityValue;
        }
        Auth auth = AuthContext.get();
        entityClass.fields().stream().forEach(field -> {
            switch (field.name()) {
                case "update_time":
                    entityValue.add(
                            new LongValue(field, LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
                    break;
                case "update_user_id":
                    if (auth != null && !StringUtils.isEmpty(auth.getId())) {
                        entityValue.add(
                                new LongValue(field, Long.valueOf(auth.getId())));
                    }
                    break;
                case "update_user_name":
                    if (auth != null && !StringUtils.isEmpty(auth.getUsername())) {
                        entityValue.add(
                                new StringValue(field, auth.getUsername()));
                    }
                    break;
                default:
            }
        });
        return entityValue;
    }

    private enum OperateType {
        SINGLE_DELETE("单个删除"),
        SINGLE_MODIFY("单个修改"),
        SINGLE_CREATE("单个创建"),
        IMPORT("导入"),
        BATCH_DELETE("批量删除"),
        BATCH_MODIFY("批量修改"),
        BATCH_CREATE("批量新增");

        private String desc;

        OperateType(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return desc;
        }
    }

}
