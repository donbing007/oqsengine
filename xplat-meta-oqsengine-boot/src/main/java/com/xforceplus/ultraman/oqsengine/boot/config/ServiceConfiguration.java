package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntitySearchServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.TransactionManagementServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置.
 *
 * @author dongbin
 * @version 0.1 2020/2/24 17:43
 * @since 1.8
 */
@Configuration
public class ServiceConfiguration {

    @Bean
    public EntitySearchService entitySearchService(@Value("${maxVisibleNumber:10000}") long maxVisibelNumber) {
        EntitySearchServiceImpl impl = new EntitySearchServiceImpl();
        impl.setMaxJoinEntityNumber(2);
        impl.setMaxJoinDriverLineNumber(1000);
        impl.setMaxVisibleTotalCount(maxVisibelNumber);
        return impl;
    }

    @Bean
    public EntityManagementService entityManagementService(
        @Value("${ignoreCDCStatusCheck:false}") boolean ignoreCDCStatusCheck,
        @Value("${sync.allowMaxSyncTimeMs:10000}") long allowMaxSyncTimeMs,
        @Value("${sync.allowMaxLiveTimeMs:3000}") long allowMaxLiveTimeMs
    ) {
        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(ignoreCDCStatusCheck);
        impl.setAllowMaxSyncTimeMs(allowMaxSyncTimeMs);
        impl.setAllowMaxLiveTimeMs(allowMaxLiveTimeMs);
        return impl;
    }

    @Bean
    public TransactionManagementService transactionManagementService() {
        return new TransactionManagementServiceImpl();
    }
}
