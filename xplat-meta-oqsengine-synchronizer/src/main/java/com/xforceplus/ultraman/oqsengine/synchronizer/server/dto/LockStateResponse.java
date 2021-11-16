package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;
import java.util.List;

/**
 * lock state.
 */
public class LockStateResponse {

    private LockState lockState;

    private List<CriticalResource> criticalResources;

    public LockStateResponse(LockState lockState, List<CriticalResource> criticalResources) {
        this.lockState = lockState;
        this.criticalResources = criticalResources;
    }

    /**
     * Lock state.
     */
    public enum LockState {

        /**
         * start.
         */
        INITED,
        /**
         * request is locked.
         */
        LOCKED,

        /**
         * request is in queue.
         */
        IN_QUEUE,

        /**
         * TODO
         * token expired.
         */
        EXPIRED,

        FATAL,

        RELEASED;
    }

    public LockState getLockState() {
        return lockState;
    }

    public List<CriticalResource> getCriticalResources() {
        return criticalResources;
    }

    @Override
    public String toString() {
        return "LockStateResponse{"
            + "lockState=" + lockState
            + ", criticalResources=" + criticalResources
            + '}';
    }
}