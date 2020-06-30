package com.xforceplus.ultraman.oqsengine.sdk.util.flow;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowRegistry {

    private ActorSystem actorSystem;

    private ActorMaterializer mat;

    public FlowRegistry(ActorMaterializer mat) {
        this.mat = mat;
    }

    Map<String, QueueFlow> map = new ConcurrentHashMap<>();

    public <T> QueueFlow<T> flow(String name){

        map.putIfAbsent(name, new QueueFlow(name, mat));

        return map.get(name);
    }
}
