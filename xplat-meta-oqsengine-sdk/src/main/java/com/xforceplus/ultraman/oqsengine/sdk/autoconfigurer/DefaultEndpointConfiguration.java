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
    @Bean
    public ModuleController moduleController(){
        return new ModuleController();
    }

    @Bean
    public EntityController entityController(){
        return new EntityController();
    }

    @Bean
    public DictController dictController(){
        return new DictController();
    }

    @Bean
    public UltFormSettingController ultFormSettingController(){
        return new UltFormSettingController();
    }

    @Bean
    public UltPageSettingController ultPageSettingController(){
        return new UltPageSettingController();
    }
}
