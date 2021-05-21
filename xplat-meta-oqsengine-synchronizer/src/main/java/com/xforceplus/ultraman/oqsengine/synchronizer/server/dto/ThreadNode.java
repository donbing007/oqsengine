package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;

import akka.actor.ActorRef;
import java.util.Objects;

/**
 * represent a thread node.
 */
public class ThreadNode {

    private String uuid;

    private ActorRef threadRef;

    public ThreadNode(String uuid, ActorRef threadRef) {
        this.uuid = uuid;
        this.threadRef = threadRef;
    }

    public String getUuid() {
        return uuid;
    }

    public ActorRef getThreadRef() {
        return threadRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreadNode that = (ThreadNode) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
