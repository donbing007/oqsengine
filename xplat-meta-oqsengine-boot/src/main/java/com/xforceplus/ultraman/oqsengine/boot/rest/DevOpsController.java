package com.xforceplus.ultraman.oqsengine.boot.rest;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.impl.DevOpsManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@RestController
public class DevOpsController {

    @Resource(name = "metaManager")
    private MetaManager metaManager;

    @Resource
    private DevOpsManagementService devOpsManagementService;

    @Resource
    private EntityManagementService entityManagementService;

    @Resource
    private EntitySearchService entitySearchService;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    //  part for oqs-meta

    /**
     * 手动导入BOCP EntityClassSyncRsp配置.
     */
    @PutMapping("/oqs/devops/import-meta/{appId}/{env}/{version}")
    public ResponseEntity<String> metaImport(@PathVariable String appId,
                                             @PathVariable String env,
                                              @PathVariable Integer version,
                                              @RequestBody String data) {
        try {
            boolean result = metaManager.dataImport(appId, env, version, data);
            return ResponseEntity.ok(result ? "success" : "less version than current use.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 关注meta信息.
     */
    @PutMapping("/oqs/devops/notice-meta/{appId}/{env}")
    public ResponseEntity<Integer> noticeMeta(@PathVariable String appId, @PathVariable String env) {
        try {
            return ResponseEntity.ok(metaManager.need(appId, env));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 查看meta信息.
     */
    @GetMapping("/oqs/devops/show-meta/{appId}")
    public ResponseEntity<MetaMetrics> showMeta(@PathVariable String appId) {
        try {
            return ResponseEntity.of(metaManager.showMeta(appId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    //  part for commitIds

    /**
     * 清理Redis中的CommitId.
     */
    @DeleteMapping("/oqs/devops/commitIds")
    public ResponseEntity<Boolean> removeCommitIds(@RequestBody Long[] ids) {
        devOpsManagementService.removeCommitIds(ids);
        return ResponseEntity.ok(true);
    }

    /**
     * part for cdc.
     */
    @PutMapping("oqs/devops/cdc/error/recover/{seqNo}")
    public ResponseEntity<Boolean> cdcErrorRecover(@PathVariable long seqNo,
                                                   @RequestBody String recoverString) throws SQLException {
        if (devOpsManagementService.cdcSendErrorRecover(seqNo, recoverString)) {
            try {
                Optional<CdcErrorTask> cdcErrorTaskOp = devOpsManagementService.queryOne(seqNo);
                FixedStatus fixedStatus = FixedStatus.FIX_ERROR;
                if (cdcErrorTaskOp.isPresent()) {
                    CdcErrorTask task = cdcErrorTaskOp.get();
                    if (task.getErrorType() == ErrorType.DATA_FORMAT_ERROR.getType()
                        && task.getOp() > OperationType.UNKNOWN.getValue()
                        && task.getEntity() > UN_KNOW_ID
                        && task.getId() > UN_KNOW_ID) {

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
                    return ResponseEntity.ok(true);
                }
            } catch (SQLException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * query cdc error by seqNo.
     */
    @GetMapping("oqs/devops/cdc/error/{seqNo}")
    public ResponseEntity<CdcErrorTask> queryCdcError(@PathVariable long seqNo) throws SQLException {
        return ResponseEntity.of(devOpsManagementService.queryOne(seqNo));
    }

    /**
     * query cdc error by condition.
     */
    @GetMapping("oqs/devops/cdc/errors")
    public ResponseEntity<Collection<CdcErrorTask>> queryCdcErrors(@RequestBody CdcErrorQueryCondition cdcErrorQueryCondition)
        throws SQLException {
        return ResponseEntity.ok(devOpsManagementService.queryCdcError(cdcErrorQueryCondition));
    }


    //   part for rebuild index

    /**
     * 重建索引.
     */
    @PostMapping("/oqs/devops/rebuild-index/{entityClassId}/{startTime}/{endTime}")
    public ResponseEntity<DevOpsTaskInfo> rebuildIndex(@PathVariable long entityClassId,
                                                       @PathVariable String start,
                                                       @PathVariable String end,
                                                       @RequestParam("profile") String profile) throws Exception {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
        if (entityClassOp.isPresent()) {
            return ResponseEntity.of(devOpsManagementService.rebuildIndex(entityClassOp.get(),
                LocalDateTime.parse(start, dateTimeFormatter),
                LocalDateTime.parse(end, dateTimeFormatter)));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 重建索引(从最后失败的位置继续).
     */
    @PutMapping("/oqs/devops/rebuild-index/{entityClassId}/{taskId}")
    public ResponseEntity<DevOpsTaskInfo> resumeIndex(@PathVariable long entityClassId,
                                                      @PathVariable String taskId,
                                                      @RequestParam("profile") String profile) throws Exception {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
        if (entityClassOp.isPresent()) {
            return ResponseEntity.of(devOpsManagementService.resumeRebuild(entityClassOp.get(), taskId));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 获取当前活动的任务列表.
     */
    @GetMapping("/oqs/devops/rebuild-index/tasks")
    ResponseEntity<Collection<DevOpsTaskInfo>> listActiveTasks(
        @RequestParam("pageIndex") long pageIndex,
        @RequestParam("pageSize")  long pageSize,
        @RequestParam("isActive")  boolean isActive) throws SQLException {
        Page page = new Page(pageIndex, pageSize);
        if (isActive) {
            return ResponseEntity.ok(devOpsManagementService.listActiveTasks(page));
        } else {
            return ResponseEntity.ok(devOpsManagementService.listAllTasks(page));
        }
    }

    /**
     * 获取当前活动任务详情.
     */
    @GetMapping("/oqs/devops/rebuild-index/{entityClassId}")
    ResponseEntity<DevOpsTaskInfo> activeTask(@PathVariable long entityClassId, @RequestParam("profile") String profile) throws SQLException {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, profile);
        if (entityClassOp.isPresent()) {
            return ResponseEntity.of(devOpsManagementService.getActiveTask(entityClassOp.get()));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 取消当前任务.
     */
    @PutMapping("/oqs/devops/rebuild-index/cancel/{taskId}")
    ResponseEntity<Boolean> cancel(String taskId) throws SQLException {
        devOpsManagementService.cancel(taskId);
        return ResponseEntity.ok(true);
    }
}
