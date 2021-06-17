package com.xforceplus.ultraman.oqsengine.boot.rest;

import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private SyncExecutor syncExecutor;

    /**
     * 手动倒入bocp EntityClassSyncRsp配置.
     */
    @PutMapping("/apis/import/meta/{appId}/{version}")
    public ResponseEntity<String> metaImport(@PathVariable String appId,
                                              @PathVariable Integer version,
                                              @RequestBody String data) {
        try {
            syncExecutor.dataImport(appId, version, data);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
