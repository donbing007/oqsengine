package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.BoNode;

/**
 * TODO
 * if has no
 * event when a boId is not present
 */
public class MetadataModuleVersionMissingEvent {

    private long moduleId;
    private String version;

    public MetadataModuleVersionMissingEvent(long moduleId, String version) {
        this.moduleId = moduleId;
        this.version = version;
    }

    public long getModuleId() {
        return moduleId;
    }

    public String getVersion() {
        return version;
    }
}
