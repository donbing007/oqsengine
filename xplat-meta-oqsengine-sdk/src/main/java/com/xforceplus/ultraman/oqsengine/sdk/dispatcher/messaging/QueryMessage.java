package com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging;

import org.springframework.core.ResolvableType;

public interface QueryMessage<T, R> extends Message<T> {

    String getQueryName();

    ResolvableType getResponseType();

//    QueryMessage<T, R> withMetaData(Map<String, ?> var1);
//
//    QueryMessage<T, R> andMetaData(Map<String, ?> var1);
}
