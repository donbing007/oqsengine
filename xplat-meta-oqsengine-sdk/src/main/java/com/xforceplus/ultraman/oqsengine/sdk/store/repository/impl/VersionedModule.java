package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import java.util.List;

/**
 * Tuple4<String, List<Long>, RingDC, Long>
 *  an immutable module
 */
public class VersionedModule {

    private final String version;

    private final RingDC ringDC;

    private final Long timestamp;

    private final List<BoNode> boNodes;

    public VersionedModule(String version, List<BoNode> boNodes, RingDC ringDC, Long timestamp) {
        this.version = version;
        this.ringDC = ringDC;
        this.timestamp = timestamp;
        this.boNodes = boNodes;
    }

    public String getVersion() {
        return version;
    }

    public RingDC getRingDC() {
        return ringDC;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public List<BoNode> getBoNodes() {
        return boNodes;
    }
}
