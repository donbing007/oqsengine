package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.ExternalServiceConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 初始化Page信息
 */
@Order(1) // @Order注解可以改变执行顺序，越小越先执行
@Component
public class UltPageInitService implements CommandLineRunner {
    final Logger logger = LoggerFactory.getLogger(UltPageInitService.class);

    @Autowired
    private AuthSearcherConfig config;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

    @Override
    public void run(String... args) throws Exception {
        logger.info("begin init pages config");
        String accessUri = ExternalServiceConfig.PfcpAccessUri();
        String url = String.format("%s/pages/init"
                , accessUri);
        Authorization auth = new Authorization();
        auth.setAppId(Long.parseLong(config.getAppId()));
//        auth.setTenantId(Long.parseLong(config.getTenant()));
        auth.setEnv(config.getEnv());
        Response<List<UltPage>> result = new Response<List<UltPage>>();
        try {
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            HttpEntity authorizeEntity = new HttpEntity(auth, headers);
            result = restTemplate.postForObject(url, authorizeEntity, Response.class);
            if (result.getResult() != null) {
                List<UltPage> ultPages = result.getResult();
                for (int i = 0; i < ultPages.size(); i++) {
                    UltPage saveUltPage = JSON.parseObject(JSON.toJSONString(ultPages.get(i)), UltPage.class);
                    pageBoMapLocalStore.save(saveUltPage);
                }
                logger.info("init pages config success");
                //将List转成Entity
//                UltPage ultPage = JSON.parseObject(JSON.toJSONString(result.getResult()),UltPage.class);
                //将数据保存到内存中
//                pageBoMapLocalStore.save(ultPage);
            }
        } catch (Exception e) {
            logger.info("init pages config faild");
            throw new Exception(
                    String.format("init pages config faild,The url is '%s'.", url));
        }
    }
}
