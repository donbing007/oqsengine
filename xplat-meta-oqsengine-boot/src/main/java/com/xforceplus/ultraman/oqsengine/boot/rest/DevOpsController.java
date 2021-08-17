package com.xforceplus.ultraman.oqsengine.boot.rest;


import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.DiscoverDevOpsService;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import java.sql.SQLException;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private DiscoverDevOpsService discoverDevOpsService;

    //  part for oqs-meta

    /**
     * 手动导入BOCP EntityClassSyncRsp配置.
     */
    @PutMapping("/oqs/devops/import-meta/{appId}/{env}/{version}")
    public ResponseEntity<String> metaImport(@PathVariable String appId,
                                             @PathVariable String env,
                                              @PathVariable Integer version,
                                              @RequestBody String data) {
        return ResponseEntity.ok(
            discoverDevOpsService.metaImport(appId, env, version, data) ? "success" : "less version than current use.");
    }

    /**
     * 关注meta信息.
     */
    @PutMapping("/oqs/devops/notice-meta/{appId}/{env}")
    public ResponseEntity<Integer> noticeMeta(@PathVariable String appId, @PathVariable String env) {
        return ResponseEntity.ok(discoverDevOpsService.noticeMeta(appId, env));
    }

    /**
     * 查看meta信息.
     */
    @GetMapping("/oqs/devops/show-meta/{appId}")
    public ResponseEntity<MetaMetrics> showMeta(@PathVariable String appId) {
        return ResponseEntity.ok(discoverDevOpsService.showMeta(appId));
    }


    //  part for commitIds

    /**
     * 清理Redis中的CommitId.
     */
    @DeleteMapping("/oqs/devops/commitIds")
    public ResponseEntity<Boolean> removeCommitIds(@RequestBody Long[] ids) {
        return ResponseEntity.ok(discoverDevOpsService.removeCommitIds(ids));
    }

    /**
     * part for cdc.
     */
    @PutMapping("oqs/devops/cdc/error/recover/{seqNo}")
    public ResponseEntity<Boolean> cdcErrorRecover(@PathVariable long seqNo,
                                                   @RequestBody String recoverString) {
        return ResponseEntity.ok(discoverDevOpsService.cdcErrorRecover(seqNo, recoverString));
    }

    /**
     * query cdc error by seqNo.
     */
    @GetMapping("oqs/devops/cdc/error/{seqNo}")
    public ResponseEntity<CdcErrorTask> queryCdcError(@PathVariable long seqNo) {
        return ResponseEntity.ok(discoverDevOpsService.queryCdcError(seqNo));
    }

    /**
     * query cdc error by condition.
     */
    @GetMapping("oqs/devops/cdc/errors")
    public ResponseEntity<Collection<CdcErrorTask>> queryCdcErrors(@RequestBody CdcErrorQueryCondition cdcErrorQueryCondition) {
        return ResponseEntity.ok(discoverDevOpsService.queryCdcErrors(cdcErrorQueryCondition));
    }


    //   part for rebuild index

    /**
     * 重建索引.
     */
    @PostMapping("/oqs/devops/rebuild-index/{entityClassId}/{startTime}/{endTime}")
    public ResponseEntity<DevOpsTaskInfo> rebuildIndex(@PathVariable long entityClassId,
                                                       @PathVariable String start,
                                                       @PathVariable String end,
                                                       @RequestParam("profile") String profile) {
        return ResponseEntity.ok(discoverDevOpsService.rebuildIndex(entityClassId, start, end, profile));
    }

    /**
     * 重建索引(从最后失败的位置继续).
     */
    @PutMapping("/oqs/devops/rebuild-index/{entityClassId}/{taskId}")
    public ResponseEntity<DevOpsTaskInfo> resumeIndex(@PathVariable long entityClassId,
                                                      @PathVariable String taskId,
                                                      @RequestParam("profile") String profile) {
        return ResponseEntity.ok(discoverDevOpsService.resumeIndex(entityClassId, taskId, profile));
    }

    /**
     * 获取当前活动的任务列表.
     */
    @GetMapping("/oqs/devops/rebuild-index/tasks")
    ResponseEntity<Collection<DevOpsTaskInfo>> listActiveTasks(
        @RequestParam("pageIndex") long pageIndex,
        @RequestParam("pageSize")  long pageSize,
        @RequestParam("isActive")  boolean isActive) throws SQLException {
        return ResponseEntity.ok(discoverDevOpsService.listActiveTasks(pageIndex, pageSize, isActive));
    }

    /**
     * 获取当前活动任务详情.
     */
    @GetMapping("/oqs/devops/rebuild-index/{entityClassId}")
    ResponseEntity<DevOpsTaskInfo> activeTask(@PathVariable long entityClassId, @RequestParam("profile") String profile) throws SQLException {
        return ResponseEntity.ok(discoverDevOpsService.activeTask(entityClassId, profile));
    }

    /**
     * 取消当前任务.
     */
    @PutMapping("/oqs/devops/rebuild-index/cancel/{taskId}")
    ResponseEntity<Boolean> cancel(String taskId) throws SQLException {
        return ResponseEntity.ok(discoverDevOpsService.cancel(taskId));
    }
}
