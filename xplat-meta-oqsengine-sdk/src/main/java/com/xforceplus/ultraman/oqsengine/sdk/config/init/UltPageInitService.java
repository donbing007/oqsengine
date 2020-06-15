package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.ExternalServiceConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.config.ExternalServiceConfig.PfcpAccessUri;

/**
 * TODO
 * 初始化Page信息
 */
@Order(1) // @Order注解可以改变执行顺序，越小越先执行
public class UltPageInitService implements InitializingBean {
    final Logger logger = LoggerFactory.getLogger(UltPageInitService.class);

    @Autowired
    private AuthSearcherConfig config;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConfigurationEngine<UltPage, JsonConfigNode> pageConfigEngine;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("begin init pages config");
        String accessUri = null;
        try {
            accessUri = PfcpAccessUri();
        } catch (NoSuchAttributeException e) {
            e.printStackTrace();
        }
        String url = String.format("%s/pages/init"
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

                List<UltPage> collect = ultPages.stream()
                        .map(x -> JSON.parseObject(JSON.toJSONString(x), UltPage.class))
                        .collect(Collectors.toList());
                pageConfigEngine.registerSource(Observable
                        .fromIterable(collect));
                //pageConfigEngine.getObservable().subscribe(x -> eventPublisher.publishEvent(new ConfigChangeEvent("PAGE", x)));
            }
        } catch (Exception e) {
            logger.info("init pages config faild");
            logger.error("{}", e);
        }
    }
}
