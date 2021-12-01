package com.xforceplus.ultraman.oqsengine.boot.grpc.devops;

import com.xforceplus.ultraman.devops.service.common.exception.DiscoverClientException;
import com.xforceplus.ultraman.devops.service.sdk.annotation.DiscoverAction;
import com.xforceplus.ultraman.devops.service.sdk.annotation.MethodParam;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsDataResponse;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQueryConfig;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQueryResponse;
import com.xforceplus.ultraman.oqsengine.devops.om.model.DevOpsQuerySummary;
import com.xforceplus.ultraman.oqsengine.devops.om.util.DevOpsOmDataUtils;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaLogs;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
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
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
@Component
public class DiscoverDevOpsService {

    private final Logger logger = LoggerFactory.getLogger(DiscoverDevOpsService.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private MetaManager metaManager;

    @Autowired
    private DevOpsManagementService devOpsManagementService;

    @Autowired
    private EntityManagementService entityManagementService;

    @Autowired
    private EntitySearchService entitySearchService;

    @Autowired
    private CommitIdStatusService commitIdStatusService;

    /**
     * 导入meta信息.
     *
     * @param appId   应用标识.
     * @param env     环境编码.
     * @param version 版本.
     * @param data    数据.
     * @return true成功, 失败.
     */
    @DiscoverAction(describe = "元数据导入", retClass = boolean.class)
    public boolean metaImport(@MethodParam(name = "appId", klass = String.class, required = true) String appId,
                              @MethodParam(name = "env", klass = String.class, required = true) String env,
                              @MethodParam(name = "version", klass = Integer.class, required = true) Integer version,
                              @MethodParam(name = "data", klass = String.class, required = true) String data) {

        try {
            return metaManager.dataImport(appId, env, version, data);
        } catch (Exception e) {
            exceptionHandle(String.format("dataImport exception, [%s-%s-%s]", appId, env, version), e);
        }
        return false;
    }

    /**
     * 关注某个meta信息.
     *
     * @param appId 应用标识.
     * @param env   环境编码.
     * @return 版本号.
     */
    @DiscoverAction(describe = "关注某一个meta信息", retClass = int.class)
    public int noticeMeta(@MethodParam(name = "appId", klass = String.class, required = true) String appId,
                          @MethodParam(name = "env", klass = String.class, required = true) String env) {
        try {
            return metaManager.need(appId, env);
        } catch (Exception e) {
            exceptionHandle(String.format("noticeMeta exception, [%s-%s]", appId, env), e);
        }

        return 0;
    }

    /**
     * 查询指定应用的元信息.
     *
     * @param appId 应用标识.
     * @return 元信息.
     */
    @DiscoverAction(describe = "查询meta信息", retClass = MetaMetrics.class)
    public MetaMetrics showMeta(@MethodParam(name = "appId", klass = String.class, required = true) String appId) {
        try {
            return metaManager.showMeta(appId).orElse(new MetaMetrics(-1, "", appId, new ArrayList<>()));
        } catch (Exception e) {
            exceptionHandle(String.format("showMeta exception, [%s]", appId), e);
        }

        return null;
    }

    /**
     * 显示meta同步日志.
     */
    @DiscoverAction(describe = "显示meta同步日志", retClass = Collection.class, retInner = MetaLogs.class)
    public Collection<MetaLogs> metaLogs() {
        try {
            return metaManager.metaLogs();
        } catch (Exception e) {
            exceptionHandle("metaLogs exception", e);
        }

        return null;
    }

    /**
     * 获取当前活动的commitIds列表.
     */
    @DiscoverAction(describe = "查询当前commitIds列表信息", retClass = List.class, retInner = Long.class)
    public Collection<Long> showAllCommitIds() {
        List<Long> result = new ArrayList<>();
        try {
            long[] ids = devOpsManagementService.showCommitIds();

            if (null != ids && ids.length > 0) {
                Arrays.stream(ids).sorted().forEach(result::add);
            }
        } catch (Exception e) {
            exceptionHandle("showAllCommitIds exception", e);
        }

        return result;
    }


    /**
     * 清理提交号.
     *
     * @param ids 目标提交号列表.
     * @return true成功, false失败.
     */
    @DiscoverAction(describe = "删除commitId", retClass = boolean.class)
    public boolean removeCommitIds(@MethodParam(name = "ids", klass = Long[].class, required = true) Long[] ids) {
        try {
            if (null == ids || ids.length == 0) {
                return false;
            }
            devOpsManagementService.removeCommitIds(ids);
            return true;
        } catch (Exception e) {
            exceptionHandle(
                    String.format("removeCommitIds exception, [%s]", Arrays.stream(ids).collect(Collectors.toList())), e);
        }
        return false;
    }

    /**
     * 修复CDC-ERROR中的错误.
     *
     * @param seqNo         序号.
     * @param recoverString 消息.
     * @return true成功, false失败.
     */
    @DiscoverAction(describe = "修复CDC-ERROR中错误的记录", retClass = boolean.class)
    public boolean cdcErrorRecover(@MethodParam(name = "seqNo", klass = long.class, required = true) long seqNo,
                                   @MethodParam(name = "recoverString", klass = String.class, required = true)
                                           String recoverString) {
        try {
            if (devOpsManagementService.cdcSendErrorRecover(seqNo, recoverString)) {
                Optional<CdcErrorTask> cdcErrorTaskOp = devOpsManagementService.queryOne(seqNo);
                FixedStatus fixedStatus = FixedStatus.FIX_ERROR;
                if (cdcErrorTaskOp.isPresent()) {
                    CdcErrorTask task = cdcErrorTaskOp.get();
                    if (task.getErrorType() == ErrorType.DATA_FORMAT_ERROR.getType()
                            && task.getOp() > OperationType.UNKNOWN.getValue()
                            && task.getEntity() > CDCConstant.UN_KNOW_ID
                            && task.getId() > CDCConstant.UN_KNOW_ID) {

                        Optional<IEntity> entityOp =
                                entitySearchService.selectOne(task.getId(), new EntityClassRef(task.getEntity(), ""));

                        com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult = null;
                        if (entityOp.isPresent()) {
                            IEntity entity = entityOp.get();
                            operationResult = entityManagementService.replace(entity);
                        } else {
                            operationResult =
                                    entityManagementService.delete(Entity.Builder.anEntity()
                                            .withId(task.getId())
                                            .withVersion(task.getVersion())
                                            .build());
                        }

                        if (operationResult.getResultStatus().equals(ResultStatus.SUCCESS)) {
                            fixedStatus = FixedStatus.FIXED;
                        }
                    }
                }

                devOpsManagementService.cdcUpdateStatus(seqNo, fixedStatus);
                if (fixedStatus == FixedStatus.FIXED) {
                    return true;
                }
            }
        } catch (Exception e) {
            exceptionHandle(String.format("cdcErrorRecover exception, [%s]", seqNo), e);
        }

        return false;
    }

    /**
     * 根据seqNo查询单条CDC-ERROR错误信息.
     *
     * @param seqNo 编号.
     * @return 查询到的任务.
     */
    @DiscoverAction(describe = "根据seqNo查询单条CDC-ERROR错误信息", retClass = CdcErrorTask.class)
    public CdcErrorTask queryCdcError(@MethodParam(name = "seqNo", klass = long.class, required = true) long seqNo) {
        try {
            return devOpsManagementService.queryOne(seqNo).orElse(null);
        } catch (Exception e) {
            exceptionHandle(String.format("queryCdcError exception, [%s]", seqNo), e);
        }

        return null;
    }

    /**
     * 根据condition查询CDC-ERROR错误信息列表.
     *
     * @param condition 查询条件.
     * @return 任务列表.
     */
    @DiscoverAction(describe = "根据condition查询CDC-ERROR错误信息列表", retClass = Collection.class, retInner = CdcErrorTask.class)
    public Collection<CdcErrorTask> queryCdcErrors(
            @MethodParam(name = "condition", klass = CdcErrorQueryCondition.class) CdcErrorQueryCondition condition) {
        try {
            return devOpsManagementService.queryCdcError(condition);
        } catch (Exception e) {
            exceptionHandle(String.format("queryCdcErrors exception, [%s]", condition.toString()), e);
        }

        return null;
    }

    /**
     * 重建索引.
     *
     * @param entityClassId 目标entityClassId.
     * @param start         开始时间.
     * @param end           结束时间.
     * @param profile       可能的替身信息.
     * @return 任务详情.
     */
    @DiscoverAction(describe = "重建索引", retClass = DevOpsTaskInfo.class)
    public DevOpsTaskInfo rebuildIndex(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "start", klass = String.class, required = true) String start,
            @MethodParam(name = "end", klass = String.class, required = true) String end,
            @MethodParam(name = "profile", klass = String.class, required = true) String profile) {
        try {
            Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
            if (entityClassOp.isPresent()) {
                return devOpsManagementService.rebuildIndex(entityClassOp.get(),
                        LocalDateTime.parse(start, dateTimeFormatter),
                        LocalDateTime.parse(end, dateTimeFormatter)).orElse(null);
            }
            return null;
        } catch (Exception e) {
            exceptionHandle(String.format("rebuildIndex exception, [%d-%s-%s-%s]",
                    entityClassId, profile == null ? "" : profile, start, end), e);
        }
        return null;
    }

    /**
     * 失败的重建索引任务在checkpoint处重试并完成余下任务.
     *
     * @param entityClassId 目标entityClass标识.
     * @param taskId        任务id.
     * @param profile       替换信息.比如租户.
     * @return 任务详情.
     */
    @DiscoverAction(describe = "失败的重建索引任务在checkpoint处重试并完成余下任务", retClass = DevOpsTaskInfo.class)
    public DevOpsTaskInfo resumeIndex(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "taskId", klass = String.class, required = true) String taskId,
            @MethodParam(name = "profile", klass = String.class, required = true) String profile) {
        try {
            Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
            if (entityClassOp.isPresent()) {
                return devOpsManagementService.resumeRebuild(entityClassOp.get(), taskId).orElse(null);
            }
            return null;
        } catch (Exception e) {
            exceptionHandle(String.format("resumeIndex exception, [%d-%s-%s]",
                    entityClassId, profile == null ? "" : profile, taskId), e);
        }

        return null;
    }

    /**
     * 重建索引任务列表页查询.
     *
     * @param pageIndex 目标页号.
     * @param pageSize  分页大小.
     * @param isActive  true 正在运行,false运行结束的.
     * @return 任务列表.
     */
    @DiscoverAction(describe = "重建索引任务列表页查询", retClass = Collection.class, retInner = DevOpsTaskInfo.class)
    public Collection<DevOpsTaskInfo> listActiveTasks(
            @MethodParam(name = "pageIndex", klass = long.class, required = true) long pageIndex,
            @MethodParam(name = "pageSize", klass = long.class, required = true) long pageSize,
            @MethodParam(name = "isActive", klass = boolean.class) boolean isActive) {
        try {
            Page page = new Page(pageIndex, pageSize);
            if (isActive) {
                return devOpsManagementService.listActiveTasks(page);
            }
            return devOpsManagementService.listAllTasks(page);
        } catch (Exception e) {
            exceptionHandle(String.format("listActiveTasks exception, [%d-%d-%s]", pageIndex, pageSize, isActive), e);
        }

        return null;
    }

    /**
     * 查询活动中的任务.
     *
     * @param entityClassId 目标元信息标识.
     * @param profile       替身.
     * @return 任务信息.
     */
    @DiscoverAction(describe = "查询活动任务", retClass = DevOpsTaskInfo.class)
    public DevOpsTaskInfo activeTask(
            @MethodParam(name = "entityClassId", klass = long.class, required = true) long entityClassId,
            @MethodParam(name = "profile", klass = String.class) String profile) {
        try {
            Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
            if (entityClassOp.isPresent()) {
                return devOpsManagementService.getActiveTask(entityClassOp.get()).orElse(null);
            }
            return null;
        } catch (Exception e) {
            exceptionHandle(String.format("query activeTask exception, [%d-%s]", entityClassId, profile), e);
        }

        return null;
    }

    /**
     * 取消任务.
     *
     * @param taskId 任务标识.
     * @return true取消成功, false取消失败.
     */
    @DiscoverAction(describe = "取消任务", retClass = boolean.class)
    public boolean cancel(
            @MethodParam(name = "taskId", klass = String.class, required = true) String taskId) {
        try {
            devOpsManagementService.cancel(taskId);
            return true;
        } catch (Exception e) {
            exceptionHandle(String.format("cancel task exception, [%s]", taskId), e);
        }
        return false;
    }

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
            exceptionHandle(String.format("selectByConditions exception, [%s]", config.getEntityClassId()), e);
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
            exceptionHandle(String.format("devops om singleCreate exception, [%s]", entityClassId), e);
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
            exceptionHandle(String.format("devops om singleModify exception, [%s-%s]", entityClassId, entityValueId), e);
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
            exceptionHandle(String.format("devops om singleDelete exception, [%s-%s]", entityClassId, entityValueId), e);
        }

        return null;
    }

    /**
     * 获得当前CDC中未同步的commitIds水位.
     * 当前redis中 un-commits 的水位.
     * 返回值为0代表redis中没有未处理的commit如果为空.
     * 当第一个值>0时代表当前总数位count.
     * 从第二个值开始代表会显示当前的un-commitIds列表(un-commitIds按从小到大排列).
     *
     * @return ids.
     */
    @DiscoverAction(describe = "获得当前CDC中未同步的commitIds水位", retClass = List.class, retInner = Long.class)
    public List<Long> getUnReadyCommits() {
        List<Long> ids = new ArrayList<>();
        try {
            long[] unReadies = commitIdStatusService.getUnreadiness();
            if (null != unReadies && unReadies.length > 0) {
                //  将数量作为ids[0]输出
                ids.add(Integer.valueOf(unReadies.length).longValue());

                //  排序数字从小到大写入ids
                Arrays.stream(unReadies)
                    .sorted()
                    //  .boxed()
                    .forEach(ids::add);
            } else {
                ids.add(0L);
            }
        } catch (Exception e) {
            exceptionHandle("get un-ready commits exception", e);
        }
        return ids;
    }


    private void exceptionHandle(String businessMessage, Exception e) {
        String error = String.format("%s, message : %s", businessMessage, e.getMessage());
        logger.warn(error);
        throw new DiscoverClientException(error);
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
