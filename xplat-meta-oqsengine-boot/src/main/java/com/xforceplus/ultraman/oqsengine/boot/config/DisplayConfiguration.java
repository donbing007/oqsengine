package com.xforceplus.ultraman.oqsengine.boot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * aa.
 *
 * @author dongbin
 * @version 0.1 2021/12/13 14:23
 * @since 1.8
 */
@Configuration
public class DisplayConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *  显示配置.
     *
     * @param type aa
     * @return aa
     */
    @Bean
    public String display(@Value("${meta.grpc.type}") String type) {

        logger.info("---------------" + type + "-----------------------");
        return "test";
    }
}
