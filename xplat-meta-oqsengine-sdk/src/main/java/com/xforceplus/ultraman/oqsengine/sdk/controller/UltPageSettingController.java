package com.xforceplus.ultraman.oqsengine.sdk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UltPageSettingController {

    /**
     * 部署页面
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/pages/{id}/deployments" )
    public String deploymentsPage(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        return tenantId+appCode+id;
    }

    /**
     * 获取页面bo列表
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/pages/{id}/bo-settings" )
    public String pageBos(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        return tenantId+appCode+id;
    }

    /**
     * 根据业务对象id获取详细json配置
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/bo-settings/{id}" )
    public String pageBoSeetings(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        return tenantId+appCode+id;
    }

}
