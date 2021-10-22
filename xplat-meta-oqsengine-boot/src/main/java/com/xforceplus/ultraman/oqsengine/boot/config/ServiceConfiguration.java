package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.impl.DevOpsManagementServiceImpl;
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

    /**
     * 实体搜索服务.
     */
    @Bean
    public EntitySearchService entitySearchService(
        @Value("${query.maxVisibleTotalCount:10000}") long maxVisibleTotalCount,
        @Value("${query.join.maxJoinEntityNumber:2}") int maxJoinEntityNumber,
        @Value("${query.join.maxJoinDriverLineNumber:1000}") long maxJoinDriverLineNumber,
        @Value("${debug.showsql:false}") boolean showSql) {
        EntitySearchServiceImpl impl = new EntitySearchServiceImpl();
        impl.setMaxJoinEntityNumber(maxJoinEntityNumber);
        impl.setMaxJoinDriverLineNumber(maxJoinDriverLineNumber);
        impl.setMaxVisibleTotalCount(maxVisibleTotalCount);
        impl.setShowResult(showSql);

        return impl;
    }

    /**
     * 实体管理服务.
     */
    @Bean
    public EntityManagementService entityManagementService(
        @Value("${sync.ignoreCDCStatusCheck:false}") boolean ignoreCDCStatusCheck,
        @Value("${sync.allowMaxLiveTimeMs:3000}") long allowMaxLiveTimeMs,
        @Value("${sync.allowMaxUnSyncCommitIdSize:30}") long allowMaxUnSyncCommitIdSize) {

        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(ignoreCDCStatusCheck);
        if (allowMaxLiveTimeMs > 0) {
            impl.setAllowMaxLiveTimeMs(allowMaxLiveTimeMs);
        }

        if (allowMaxUnSyncCommitIdSize > 0) {
            impl.setAllowMaxUnSyncCommitIdSize(allowMaxUnSyncCommitIdSize);
        }
        return impl;
    }

    @Bean
    public DevOpsManagementService devOpsManagementService() {
        return new DevOpsManagementServiceImpl();
    }

    @Bean
    public TransactionManagementService transactionManagementService() {
        return new TransactionManagementServiceImpl();
    }

}
