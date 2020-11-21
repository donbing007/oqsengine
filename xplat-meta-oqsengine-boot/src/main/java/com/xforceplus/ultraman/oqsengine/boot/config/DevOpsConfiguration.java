package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.devops.DevOpsStorage;
import com.xforceplus.ultraman.oqsengine.devops.SQLDevOpsStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * desc :
 * name : DevOpsConfiguration
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public class DevOpsConfiguration {
    @Bean
    public DevOpsStorage devOpsStorage(
            @Value("${storage.devOps.cdc.errors.name:cdcerrors}") String cdcErrorName,
            @Value("${storage.devOps.maxQueryTimeMs:0}") long maxQueryTimeMs) {

        SQLDevOpsStorage storage = new SQLDevOpsStorage();
        storage.setQueryTimeout(maxQueryTimeMs);
        storage.setCdcErrorRecordTable(cdcErrorName);
        return storage;
    }
}
