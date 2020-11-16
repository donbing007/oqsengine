package com.xforceplus.ultraman.oqsengine.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.boot.cdc.CDCMetricsCallbackToEvent;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CDCStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongbin
 * @version 0.1 2020/11/16 16:05
 * @since 1.8
 */
@Configuration
public class StatusConfiguration {


    @Bean
    public CommitIdStatusService commitIdStatusService() {
        return new CommitIdStatusServiceImpl();
    }

    @Bean
    public CDCStatusService cdcStatusService() {
        return new CDCStatusServiceImpl();
    }

    @Bean("cdcCallback")
    public CDCMetricsCallback cdcMetricsCallback(ApplicationEventPublisher publisher, StatusService statusService
        , @Value("${redis.cdc.key:cdcmetric}") String key, ObjectMapper mapper) {
        return new CDCMetricsCallbackToEvent(publisher, statusService, key, mapper);
    }
}
