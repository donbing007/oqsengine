package com.xforceplus.ultraman.oqsengine.sdk.config.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.xforceplus.ultraman.config.Versioned;
import com.xforceplus.ultraman.config.json.impl.DefaultJsonConfigNode;

import java.util.Optional;

/**
 * versioned json config
 */
public class VersionedJsonConfig extends DefaultJsonConfigNode implements Versioned {

    private String version;

    private Integer versionOrder;

    public VersionedJsonConfig(String version, String type, String id, JsonNode jsonNode, Integer versionOrder) {
        super(id, type, jsonNode);
        this.version = version;
        this.versionOrder = versionOrder;
    }

    @Override
    public Long getVersion() {
        return Optional.ofNullable(versionOrder).map(Integer::longValue).orElse(null);
    }

    @Override
    public String getVersionStr() {
        return version;
    }
}
