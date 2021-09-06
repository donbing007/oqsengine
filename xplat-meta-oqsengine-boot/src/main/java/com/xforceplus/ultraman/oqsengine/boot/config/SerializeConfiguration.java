package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 序列化配置.
 *
 * @author dongbin
 * @version 0.1 2021/08/26 11:18
 * @since 1.8
 */
@Configuration
public class SerializeConfiguration {

    @Bean
    public SerializeStrategy serializeStrategy() {
        return new HessianSerializeStrategy();
    }
}
