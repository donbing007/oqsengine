package com.xforceplus.ultraman.oqsengine.synchronizer.server.impl;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResourceHandler;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.ThreadNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * id resource handler. not using now
 */
public class IdResourceHandler implements CriticalResourceHandler<IdCriticalResource> {

    /**
     * TODO redis.
     */
    Map<Long, ThreadNode> exclusiveUUID = new ConcurrentHashMap<>();
    Map<ThreadNode, List<Long>> resourceOwner = new HashMap<>();
    Map<Long, Integer> resourceCount = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(IdCriticalResource resource, ThreadNode node) {

        System.out.println("Lock Current Thread is: " + Thread.currentThread());
        ThreadNode currentThreadNode = exclusiveUUID.get(resource.getRes());
        if (currentThreadNode == null) {

            exclusiveUUID.put(resource.getRes(), node);

            resourceOwner.compute(node, (k, v) -> {
                if (v == null) {
                    List<Long> ids = new ArrayList<>();
                    ids.add(resource.getRes());
                    return ids;
                } else {
                    v.add(resource.getRes());
                    return v;
                }
            });
            return true;
        } else {
            if (node.equals(currentThreadNode)) {
                //重入
                resourceCount.computeIfPresent(resource.getRes(), (k, v) -> v + 1);
                return true;
            } else {
                //addToWaiter
                return false;
            }
        }
    }

    @Override
    public boolean tryRelease(IdCriticalResource resource, ThreadNode node) {

        ThreadNode currentThreadNode = exclusiveUUID.get(resource.getRes());
        if (currentThreadNode == null) {
            throw new RuntimeException("release a non-suitable");
        } else {
            if (currentThreadNode.equals(node)) {
                resourceCount.computeIfPresent(resource.getRes(), (k, v) -> {
                    v = v - 1;
                    if (v == 0) {
                        return null;
                    } else {
                        return v;
                    }
                });

                resourceOwner.computeIfPresent(node, (k, v) -> {
                    v.remove(resource.getRes());
                    if (v.isEmpty()) {
                        return null;
                    } else {
                        return v;
                    }
                });

                return true;
            }
        }

        return false;
    }
}
