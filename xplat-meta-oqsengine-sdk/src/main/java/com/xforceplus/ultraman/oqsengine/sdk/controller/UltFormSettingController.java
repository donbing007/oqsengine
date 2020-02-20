package com.xforceplus.ultraman.oqsengine.sdk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UltFormSettingController {

    /**
     * 部署动态表单
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/forms/{id}/deployments" )
    public String deploymentsForm(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        return tenantId+appCode+id;
    }

    /**
     * 根据表单id获取详细json配置
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/form-settings/{id}" )
    public String pageBoSeetings(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        return tenantId+appCode+id;
    }

}
