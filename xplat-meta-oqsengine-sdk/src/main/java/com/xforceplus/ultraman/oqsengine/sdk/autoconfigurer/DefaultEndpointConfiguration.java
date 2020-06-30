package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import com.xforceplus.ultraman.oqsengine.sdk.controller.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * default endpoints auto-configuration
 *
 */
@ConditionalOnProperty(value = "xplat.oqsengine.sdk.endpoints.enabled", matchIfMissing = true)
public class DefaultEndpointConfiguration {


    //controller
    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled")
    @Bean
    public ModuleController moduleController(){
        return new ModuleController();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled")
    @Bean
    public DictController dictController(){
        return new DictController();
    }


    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled")
    @Bean
    public UltFormSettingController ultFormSettingController(){
        return new UltFormSettingController();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled")
    @Bean
    public UltPageSettingController ultPageSettingController(){
        return new UltPageSettingController();
    }

    @Bean
    public EntityController entityController(){
        return new EntityController();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled")
    @Bean
    public ConfigController configController(){ return new ConfigController(); }
}
