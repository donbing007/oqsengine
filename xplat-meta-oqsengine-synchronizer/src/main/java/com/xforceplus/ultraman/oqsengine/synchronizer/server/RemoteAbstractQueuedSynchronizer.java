package com.xforceplus.ultraman.oqsengine.synchronizer.server;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.CriticalResourceStateResponse;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.ThreadNode;
import io.vavr.control.Either;

import java.util.List;

/**
 * something like abstract queued synchronizer
 * a complicated state manager.
 */
public abstract class RemoteAbstractQueuedSynchronizer {

    public abstract CriticalResourceStateResponse checkResourceState(CriticalResource criticalResource);

    public abstract void addWaiter(CriticalResource criticalResource, ThreadNode threadNode);

    public abstract void wakeup(List<CriticalResource> criticalResources);

    public abstract Either<CriticalResource, Boolean> tryLock(List<CriticalResource> resources, ThreadNode threadNode);

    public abstract boolean tryRelease(List<CriticalResource> resources, ThreadNode threadNode);
}
