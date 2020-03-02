package com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging;

import org.springframework.core.ResolvableType;

import java.util.Map;

public class GenericQueryMessage<T, R> implements QueryMessage<T, R> {

    private static final long serialVersionUID = -3908412412867063631L;
    private final String queryName;
    private final ResolvableType responseType ;
    private final Message<T> message;

    public GenericQueryMessage(T payload, ResolvableType responseType) {
        this(payload, payload.getClass().getName(), responseType);
    }

    public GenericQueryMessage(T payload, String queryName, ResolvableType responseType) {
        this((Message)(new GenericMessage(payload, payload.getClass())), queryName, responseType);
    }

    public GenericQueryMessage(Message<T> message, String queryName, ResolvableType responseType) {
        this.responseType = responseType;
        this.queryName = queryName;
        this.message = message;
    }

    public String getQueryName() {
        return this.queryName;
    }

    @Override
    public ResolvableType getResponseType() {
        return responseType;
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
        return message.getPayload();
    }

    @Override
    public Class<T> getPayloadType() {
        return message.getPayloadType();
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
