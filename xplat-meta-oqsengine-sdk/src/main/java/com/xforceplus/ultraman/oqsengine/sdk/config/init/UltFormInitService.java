package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.ExternalServiceConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 初始化Form信息
 *
 * @author admin
 */
@Order(2) // @Order注解可以改变执行顺序，越小越先执行
@Component
public class UltFormInitService implements CommandLineRunner {
    final Logger logger = LoggerFactory.getLogger(UltFormInitService.class);

    @Autowired
    private AuthSearcherConfig config;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FormBoMapLocalStore formBoMapLocalStore;

    @Override
    public void run(String... args) throws Exception {
        logger.info("begin init forms config");
        String accessUri = ExternalServiceConfig.PfcpAccessUri();
        String url = String.format("%s/forms/init"
                , accessUri);
        Authorization auth = new Authorization();
        auth.setAppId(Long.parseLong(config.getAppId()));
        auth.setEnv(config.getEnv());
        Response<List<UltForm>> result = new Response<List<UltForm>>();
        try {
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            HttpEntity authorizeEntity = new HttpEntity(auth, headers);
            result = restTemplate.postForObject(url, authorizeEntity, Response.class);
            if (result.getResult() != null) {
                List<UltForm> ultForms = result.getResult();
                for (int i = 0; i < ultForms.size(); i++) {
                    UltForm saveUltForm = JSON.parseObject(JSON.toJSONString(ultForms.get(i)), UltForm.class);
                    formBoMapLocalStore.save(saveUltForm);
                }
                logger.info("init forms config success");
            }
        } catch (Exception e) {
            logger.info("init forms config faild");
            throw new Exception(
                    String.format("init forms config faild,The url is '%s'.", url));
        }

    }
}
