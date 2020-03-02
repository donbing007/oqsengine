package com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging;

import java.util.Map;

public class GenericMessage<T> implements Message<T> {
    private static final long serialVersionUID = 7937214711724527316L;

    private final Class<T> payloadType;
    private final T payload;

    public GenericMessage(T payload, Class<T> payloadType) {
        this.payloadType = payloadType;
        this.payload = payload;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public MetaData getMetaData() {
        return null;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public Class<T> getPayloadType() {
        return payloadType;
    }

    @Override
    public Message<T> withMetaData(Map<String, ?> var1) {
        return null;
    }

    @Override
    public Message<T> andMetaData(Map<String, ?> var1) {
        return null;
    }
}