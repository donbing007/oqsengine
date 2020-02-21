package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class UltFormSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FormBoMapLocalStore formBoMapLocalStore;

    /**
     * 部署动态表单
     * @return
     */
    @GetMapping("/api/{tenantId}/{appCode}/forms/{id}/deployments" )
    public Object deploymentsForm(@PathVariable String tenantId,@PathVariable String appCode,@PathVariable String id) {
        String accessUri = "http://localhost:8080";
        String url = String.format("%s/pages/%s/deployments"
                , accessUri
                , id);
        ResponseEntity<PageBo> result = restTemplate.getForEntity(url,PageBo.class);
        System.out.println(result);
        return result;
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
