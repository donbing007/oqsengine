package com.xforceplus.ultraman.oqsengine.meta.annotation;

import com.xforceplus.ultraman.oqsengine.meta.config.ClientConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * desc :
 * name : EnableMetaSyncClient
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ ClientConfiguration.class })
public @interface EnableMetaSyncClient {
}
