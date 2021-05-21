package com.xforceplus.ultraman.oqsengine.meta.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * desc :
 * name : BindGRpcService
 *
 * @author : xujia
 * date : 2021/3/23
 * @since : 1.8
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
@Inherited
public @interface BindGRpcService {
}
