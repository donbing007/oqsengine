package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.devops.DevOpsStorage;
import com.xforceplus.ultraman.oqsengine.devops.SQLDevOpsStorage;
import com.xforceplus.ultraman.oqsengine.devops.executor.rebuild.DevOpsRebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.executor.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.utils.LockExecutor;
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
            @Value("${devops.task.split:10}") int splitPart,
            @Value("${devops.task.max.queue.size:2000}") int maxQueueSize,
            @Value("${devops.task.execution.timeout:30000}") int executionTimeout,
            @Value("${devops.task.update.frequency:100}") int updateFrequency,
            @Value("${devops.task.cache.expire:30}") long cacheExpireTime,
            @Value("${devops.task.cache.maxsize:500}") long cacheMaxSize,
            @Value("${storage.devOps.maxQueryTimeMs:3000}") int taskTimeout,
            @Value("${storage.task.page.size:1000}") int pageSize) {
        return new DevOpsRebuildIndexExecutor(splitPart, maxQueueSize, executionTimeout,
                updateFrequency, cacheExpireTime, cacheMaxSize, taskTimeout, pageSize);
    }
    @Bean(name = "lockExecutor")
    public LockExecutor lockExecutor() {
        return new LockExecutor();
    }

    @Bean
    public DevOpsStorage devOpsStorage(
            @Value("${storage.devOps.cdc.errors.name:cdcerrors}") String cdcErrorName,
            @Value("${storage.devOps.maxQueryTimeMs:3000}") long maxQueryTimeMs) {

        SQLDevOpsStorage storage = new SQLDevOpsStorage();
        storage.setQueryTimeout(maxQueryTimeMs);
        storage.setCdcErrorRecordTable(cdcErrorName);
        return storage;
    }
}
