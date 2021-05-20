package com.xforceplus.ultraman.oqsengine.synchronizer.server.thread;

import akka.actor.AbstractActor;

/**
 * a critical resource manager associated with state-service.
 */
public class CriticalResourceManager extends AbstractActor {


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReleaseLock.class, x -> {

                })

                .build();
    }

    /**
     * release lock
     */
    public static class ReleaseLock {

        private String uuid;

        public ReleaseLock(String uuid) {
            this.uuid = uuid;
        }
    }
}
