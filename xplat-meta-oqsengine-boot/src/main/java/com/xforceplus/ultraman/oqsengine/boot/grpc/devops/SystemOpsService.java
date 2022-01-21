package com.xforceplus.ultraman.oqsengine.boot.grpc.devops;

import com.xforceplus.ultraman.devops.service.sdk.annotation.DiscoverAction;
import com.xforceplus.ultraman.devops.service.sdk.annotation.MethodParam;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.PrintErrorHelper;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
@Component
public class SystemOpsService {

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
            return metaManager.metaImport(appId, env, version, data);
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle(String.format("dataImport exception, [%s-%s-%s]", appId, env, version), e);
        }
        return false;
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
            PrintErrorHelper.exceptionHandle(String.format("showMeta exception, [%s]", appId), e);
        }

        return null;
    }

    /**
     * 显示meta同步日志.
     */
    @DiscoverAction(describe = "显示meta同步日志", retClass = Collection.class, retInner = MetricsLog.class)
    public Collection<MetricsLog> metaLogs(
        @MethodParam(name = "type", klass = String.class, required = true) String type) {
        try {
            return metaManager.metaLogs(MetricsLog.ShowType.getInstance(type));
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle("metaLogs exception", e);
        }

        return null;
    }


    /**
     * 重置meta下appId的env.
     *
     * @param appId 应用id.
     * @param env   环境标示.
     * @return 当前同步的版本.
     */
    @DiscoverAction(describe = "刷新当前meta下appId的env", retClass = int.class)
    public int resetMeta(
        @MethodParam(name = "appId", klass = String.class, required = true) String appId,
        @MethodParam(name = "env", klass = String.class, required = true) String env) {
        try {
            return metaManager.reset(appId, env);
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle(String.format("reset meta exception, [%s-%s]", appId, env), e);
        }
        return -1;
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
            //  将数量作为ids[0]输出
            ids.add(commitIdStatusService.getMin().orElse(0L));
            ids.add(commitIdStatusService.getMax().orElse(0L));
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle("get un-ready commits exception", e);
        }
        return ids;
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
            PrintErrorHelper.exceptionHandle(
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

                        OqsResult<IEntity> result =
                            entitySearchService.selectOne(task.getId(), new EntityClassRef(task.getEntity(), ""));

                        OqsResult oqsResult = null;
                        if (result.getValue().isPresent()) {
                            IEntity entity = result.getValue().get();
                            oqsResult = entityManagementService.replace(entity);
                        } else {
                            oqsResult =
                                entityManagementService.delete(Entity.Builder.anEntity()
                                    .withId(task.getId())
                                    .withVersion(task.getVersion())
                                    .build());
                        }

                        if (oqsResult.getResultStatus().equals(ResultStatus.SUCCESS)) {
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
            PrintErrorHelper.exceptionHandle(String.format("cdcErrorRecover exception, [%s]", seqNo), e);
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
            PrintErrorHelper.exceptionHandle(String.format("queryCdcError exception, [%s]", seqNo), e);
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
            PrintErrorHelper.exceptionHandle(String.format("queryCdcErrors exception, [%s]", condition.toString()), e);
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
            PrintErrorHelper.exceptionHandle(String.format("rebuildIndex exception, [%d-%s-%s-%s]",
                entityClassId, profile == null ? "" : profile, start, end), e);
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
            PrintErrorHelper.exceptionHandle(
                String.format("listActiveTasks exception, [%d-%d-%s]", pageIndex, pageSize, isActive), e);
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
            PrintErrorHelper.exceptionHandle(
                String.format("query activeTask exception, [%d-%s]", entityClassId, profile), e);
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
            PrintErrorHelper.exceptionHandle(String.format("cancel task exception, [%s]", taskId), e);
        }
        return false;
    }

    /**
     * 获取meta同步日志.
     *
     * @param type 需要查看的日志ALL/INFO/ERROR类型.
     * @return 日志集合.
     */
    @DiscoverAction(describe = "获取meta同步日志", retClass = List.class, retInner = MetricsLog.class)
    public Collection<MetricsLog> showMetaLogs(
        @MethodParam(name = "type", klass = String.class, required = false) String type) {
        try {
            return metaManager.metaLogs(MetricsLog.ShowType.getInstance(type));
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle("show metaLogs exception.", e);
        }
        return null;
    }

    /**
     * 获取当前oqs下所有的app->env.
     *
     * @return app->env pairs.
     */
    @DiscoverAction(describe = "获取当前oqs下所有的app", retClass = Map.class)
    public Map<String, String> showApplications() {
        try {
            return metaManager.showApplications();
        } catch (Exception e) {
            PrintErrorHelper.exceptionHandle("show applications exception.", e);
        }
        return null;
    }

}
