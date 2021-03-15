package com.xforceplus.ultraman.oqsengine.changelog.gateway;

public interface Gateway<T, E> extends CommandGateway<T>, EventGateway<E> {
}
