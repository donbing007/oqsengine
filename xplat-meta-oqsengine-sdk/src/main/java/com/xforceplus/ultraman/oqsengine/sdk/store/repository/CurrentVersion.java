package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import java.util.Map;

/**
 * current version is a key-value of all module-version pairs
 */
public class CurrentVersion {

    Map<String, String> versionMapping;

    public Map<String, String> getVersionMapping() {
        return versionMapping;
    }

    public void setVersionMapping(Map<String, String> versionMapping) {
        this.versionMapping = versionMapping;
    }
}
