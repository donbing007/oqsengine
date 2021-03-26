package com.xforceplus.ultraman.oqsengine.changelog.gateway;

import java.util.Map;

public interface CommandGateway<T> {

    /**
     * side-effect command
     */
    void fireAndForget(T t, Map<String, Object> context);
}
