package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;

import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.Message;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Documented
public @interface MessageHandler {

    Class<? extends Message> messageType() default Message.class;

    Class<?> payloadType() default Object.class;
}
