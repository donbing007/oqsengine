package com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging;

import java.io.Serializable;
import java.util.Map;

public interface Message<T> extends Serializable {

    String getIdentifier();

    MetaData getMetaData();

    T getPayload();

    Class<T> getPayloadType();

    Message<T> withMetaData(Map<String, ?> var1);

    Message<T> andMetaData(Map<String, ?> var1);
}
