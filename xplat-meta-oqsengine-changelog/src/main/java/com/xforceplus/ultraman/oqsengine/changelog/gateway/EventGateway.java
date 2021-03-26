package com.xforceplus.ultraman.oqsengine.changelog.gateway;

public interface EventGateway<T> {

    void dispatchEvent(T t);

}
