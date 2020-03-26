package com.xforceplus.ultraman.oqsengine.sdk.service.flow;

import java.util.List;

public class FlowNodeBuilder<T, U> {

    private Shapes<T> currentShapes;

    private List<Flow> flow;

    public FlowNodeBuilder(Shapes currentShapes){
        this.currentShapes = currentShapes;
    }
}
