package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import java.util.Map;

/**
 * current version is a key-value of all module-version pairs
 */
public class CurrentVersion {

    Map<Long, String> versionMapping;

    public CurrentVersion(Map<Long, String> versionMapping) {
        this.versionMapping = versionMapping;
    }

    public Map<Long, String> getVersionMapping() {
        return versionMapping;
    }

    public void setVersionMapping(Map<Long, String> versionMapping) {
        this.versionMapping = versionMapping;
    }
}
