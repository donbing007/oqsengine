package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.ExternalServiceConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.naming.directory.NoSuchAttributeException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 初始化Form信息
 *
 * @author admin
 */
@Order(2) // @Order注解可以改变执行顺序，越小越先执行
public class UltFormInitService implements SmartInitializingSingleton {
    final Logger logger = LoggerFactory.getLogger(UltFormInitService.class);

    @Autowired
    private AuthSearcherConfig config;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConfigurationEngine<UltForm, JsonConfigNode> formConfigEngine;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void afterSingletonsInstantiated() {

        logger.info("begin init forms config");
        String accessUri = null;
        try {
            accessUri = ExternalServiceConfig.PfcpAccessUri();
        } catch (NoSuchAttributeException e) {
            e.printStackTrace();
        }
        String url = String.format("%s/forms/init"
                , accessUri);
        Authorization auth = new Authorization();
        auth.setAppId(Long.parseLong(config.getAppId()));
        auth.setEnv(config.getEnv());
        Response<List<Map>> result = new Response<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            HttpEntity authorizeEntity = new HttpEntity(auth, headers);
            result = restTemplate.postForObject(url, authorizeEntity, Response.class);
            if (result.getResult() != null) {

                List<Map> ultPages = result.getResult();

                List<UltForm> collect = ultPages.stream()
                        .map(x -> JSON.parseObject(JSON.toJSONString(x), UltForm.class))
                        .collect(Collectors.toList());
                formConfigEngine.registerSource(Observable
                        .fromIterable(collect));
                formConfigEngine.getObservable().subscribe(x -> eventPublisher.publishEvent(new ConfigChangeEvent(ConfigType.FORM.name(), x)));
                logger.info("init forms config success");
                    //UltForm saveUltForm = JSON.parseObject(JSON.toJSONString(ultForms.get(i)), UltForm.class);
                    //formBoMapLocalStore.save(saveUltForm);
            }
        } catch (Exception e) {
            logger.info("init forms config faild {}", e);
        }
    }
}
