package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import com.xforceplus.ultraman.oqsengine.sdk.handler.DefaultStaticUIService;
import com.xforceplus.ultraman.oqsengine.sdk.steady.StaticServerLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * static configuration
 */
@Configuration
@ConditionalOnProperty("xplat.oqsengine.sdk.static.enabled")
public class StaticAutoConfiguration {

    @Bean
    public DefaultStaticUIService uiService() {
        return new DefaultStaticUIService();
    }

    @Bean
    public StaticServerLoader serverLoader(@Value("${xplat.oqsengine.sdk.static.package-name}") String packageName, ApplicationContext context) {
        return new StaticServerLoader(packageName, context);
    }
}
