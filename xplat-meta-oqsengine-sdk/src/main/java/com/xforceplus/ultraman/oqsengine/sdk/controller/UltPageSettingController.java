package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class UltPageSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

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
