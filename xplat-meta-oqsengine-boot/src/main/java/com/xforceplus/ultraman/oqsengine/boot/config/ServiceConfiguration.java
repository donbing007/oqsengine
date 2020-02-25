package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntitySearchServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.TransactionManagementServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置.
 * @author dongbin
 * @version 0.1 2020/2/24 17:43
 * @since 1.8
 */
//@Configuration
public class ServiceConfiguration {

    @Bean
    public EntitySearchService entitySearchService() {
        return new EntitySearchServiceImpl();
    }

    @Bean
    public EntityManagementService entityManagementService() {
        return new EntityManagementServiceImpl();
    }

    @Bean
    public TransactionManagementService transactionManagementService() {
        return new TransactionManagementServiceImpl();
    }
}
