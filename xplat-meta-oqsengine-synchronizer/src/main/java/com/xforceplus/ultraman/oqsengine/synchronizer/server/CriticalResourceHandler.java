package com.xforceplus.ultraman.oqsengine.synchronizer.server;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.ThreadNode;

/**
 * a resource related handler.
 */
public interface CriticalResourceHandler<T extends CriticalResource> {

    boolean tryLock(T resource, ThreadNode node);

    boolean tryRelease(T resource, ThreadNode node);
}
