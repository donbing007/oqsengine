package com.xforceplus.ultraman.oqsengine.testcontainer.junit4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dongbin
 * @version 0.1 2020/12/25 17:31
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DependentContainers {

    ContainerType[] value();
}
