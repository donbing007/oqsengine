package com.xforceplus.ultraman.oqsengine.synchronizer.server;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.CriticalResourceKey;

/**
 * TODO critical resource
 * criticalResource.
 */
public interface CriticalResource<T> extends Comparable<CriticalResource<T>> {

    CriticalResourceKey getKey();

    ResType getType();

    T getRes();

    enum ResType {
        ID;
    }
}
