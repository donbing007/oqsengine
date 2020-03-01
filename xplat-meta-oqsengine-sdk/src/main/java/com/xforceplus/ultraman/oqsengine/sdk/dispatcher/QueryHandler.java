package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;


import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.QueryMessage;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@MessageHandler(
        messageType = QueryMessage.class
)
public @interface QueryHandler {

    String condition() default "";

    String queryName() default "";

    boolean isDefault() default false;

}
