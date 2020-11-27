package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.devops.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.SQLCdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.DevOpsRebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.LockExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc :
 * name : DevOpsConfiguration
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
@Configuration
public class DevOpsConfiguration {

    @Bean
    public RebuildIndexExecutor devOpsRebuildIndexExecutor(
        @Value("${storage.devOps.task.split:10}") int splitPart,
        @Value("${storage.devOps.task.max.queue.size:2000}") int maxQueueSize,
        @Value("${storage.devOps.task.execution.timeout:30000}") int executionTimeout,
        @Value("${storage.devOps.task.update.frequency:100}") int updateFrequency,
        @Value("${storage.devOps.task.cache.expire:30}") long cacheExpireTime,
        @Value("${storage.devOps.task.cache.maxsize:500}") long cacheMaxSize,
        @Value("${storage.devOps.maxQueryTimeMs:3000}") int taskTimeout,
        @Value("${storage.devOps.task.page.size:1000}") int pageSize) {
        return new DevOpsRebuildIndexExecutor(splitPart, maxQueueSize, executionTimeout,
            updateFrequency, cacheExpireTime, cacheMaxSize, taskTimeout, pageSize);
    }
    @Bean(name = "lockExecutor")
    public LockExecutor lockExecutor() {
        return new LockExecutor();
    }

    @Bean
    public CdcErrorStorage cdcErrorStorage(
        @Value("${storage.devOps.cdc.errors.name:cdcerrors}") String cdcErrorName,
        @Value("${storage.devOps.maxQueryTimeMs:3000}") long maxQueryTimeMs) {

        SQLCdcErrorStorage storage = new SQLCdcErrorStorage();
        storage.setQueryTimeout(maxQueryTimeMs);
        storage.setCdcErrorRecordTable(cdcErrorName);
        return storage;
    }
}
