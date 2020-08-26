package com.xforceplus.ultraman.oqsengine.sdk.configuration;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        System.setProperty("BOCP_SERVER_HOST", "ultraman.xforcecloud.com");
        System.setProperty("BOCP_SERVER_PORT", "23120");
//        System.setProperty("OQSENGINE_SERVER_HOST", "localhost");
//        System.setProperty("OQSENGINE_SERVER_PORT", "8081");
    }
}