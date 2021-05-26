package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;


import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;

/**
 * resource state response.
 */
public class CriticalResourceStateResponse {

    /**
     * requested critical resource.
     */
    private CriticalResource criticalResource;

    private CriticalResourceState state;

    private ResourceHolder resourceHolder;

    enum CriticalResourceState {
        /**
         * locked.
         */
        LOCKED,

        /**
         * idle.
         */
        IDLE;
    }

    public CriticalResourceStateResponse(CriticalResource criticalResource, CriticalResourceState state) {
        this.criticalResource = criticalResource;
        this.state = state;
    }

    public CriticalResource getCriticalResource() {
        return criticalResource;
    }

    public CriticalResourceState getState() {
        return state;
    }
}
