package com.xforceplus.ultraman.oqsengine.boot.rest;

import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.SystemOpsService;
import com.xforceplus.ultraman.oqsengine.boot.rest.dto.RebuildIndexes;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
@RestController
public class DevOpsController {

    @Autowired
    private SystemOpsService systemOpsService;

    @Autowired
    private MetaManager metaManager;

    /**
     * 重建索引.
     */
    @PostMapping(value = "/oqs/devops/rebuild-index/{entityClassId}/{start}/{end}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DevOpsTaskInfo> rebuildIndex(@PathVariable long entityClassId,
                                                       @PathVariable String start,
                                                       @PathVariable String end) {
        return ResponseEntity.ok(systemOpsService.rebuildIndex(entityClassId, start, end));
    }

    /**
     * 批量重建索引.
     */
    @PostMapping(value = "/oqs/devops/rebuild-indexes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<DevOpsTaskInfo>> rebuildIndexes(@RequestBody RebuildIndexes rebuildIndexes) {

        return ResponseEntity.ok(
            systemOpsService.rebuildIndexes(rebuildIndexes.getAppId(),
                rebuildIndexes.getEntityClassIds(), rebuildIndexes.getStart(), rebuildIndexes.getEnd(), rebuildIndexes.getEnv())
        );
    }

    /**
     * 取消任务.
     */
    @PutMapping(value = "/oqs/devops/rebuild-index/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> cancelTask(@RequestBody List<String> ids) {

        int canceledSize = 0;
        for (String id : ids) {
            if (systemOpsService.cancel(id)) {
                canceledSize++;
            }
        }

        return ResponseEntity.ok(canceledSize == ids.size());
    }

    /**
     * 获取当前的(活动)任务列表.
     */
    @GetMapping(value = "/oqs/devops/rebuild-index/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Collection<DevOpsTaskInfo>> queryTasks(
        @RequestParam("pageIndex") long pageIndex,
        @RequestParam("pageSize")  long pageSize,
        @RequestParam(value = "isActive", required = false)  boolean isActive) {
        return ResponseEntity.ok(systemOpsService.listActiveTasks(pageIndex, pageSize, isActive));
    }
}
