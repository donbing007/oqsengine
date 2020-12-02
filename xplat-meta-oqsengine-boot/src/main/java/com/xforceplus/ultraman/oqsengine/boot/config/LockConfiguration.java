package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.common.lock.ResourceLocker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc :
 * name : LockConfiguration
 *
 * @author : xujia
 * date : 2020/11/27
 * @since : 1.8
 */
@Configuration
public class LockConfiguration {

    @Bean
    public ResourceLocker locker() {
        return new LocalResourceLocker();
    }
}
