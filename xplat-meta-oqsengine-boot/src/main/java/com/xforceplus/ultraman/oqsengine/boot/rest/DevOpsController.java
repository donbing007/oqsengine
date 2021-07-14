package com.xforceplus.ultraman.oqsengine.boot.rest;

import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import javax.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@RestController
public class DevOpsController {

    @Resource(name = "grpcSyncExecutor")
    private StorageMetaManager storageMetaManager;

    /**
     * 手动导入BOCP EntityClassSyncRsp配置.
     */
    @PutMapping("/oqs/devops/import-meta/{appId}/{version}")
    public ResponseEntity<String> metaImport(@PathVariable String appId,
                                              @PathVariable Integer version,
                                              @RequestBody String data) {
        try {
            boolean result = storageMetaManager.dataImport(appId, version, data);
            return ResponseEntity.ok(result ? "success" : "less version than current use.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/oqs/devops/show-meta/{appId}")
    public ResponseEntity<MetaMetrics> showMeta(@PathVariable String appId) {
        try {
            return ResponseEntity.of(storageMetaManager.showMeta(appId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
