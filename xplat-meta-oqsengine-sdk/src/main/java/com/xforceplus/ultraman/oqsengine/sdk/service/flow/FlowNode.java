package com.xforceplus.ultraman.oqsengine.sdk.service.flow;

public interface FlowNode<R> {

    FlowNode via(DSLFlow flow);

    Shapes end();
}
