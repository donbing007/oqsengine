package com.xforceplus.ultraman.oqsengine.boot.rest;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import java.util.Optional;
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

    @Resource(name = "metaManager")
    private MetaManager metaManager;

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
    @GetMapping("/oqs/devops/notice-meta/{appId}/{env}")
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
}
