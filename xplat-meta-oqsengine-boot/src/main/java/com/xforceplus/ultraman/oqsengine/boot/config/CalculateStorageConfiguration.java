package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculate.ActualCalculateStorage;
import com.xforceplus.ultraman.oqsengine.calculate.CalculateStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalculateStorageConfiguration {

    @Bean
    public CalculateStorage calculateStorage() {
        return new ActualCalculateStorage();
    }

}
