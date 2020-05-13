package com.xforceplus.ultraman.oqsengine.sdk.command;

import java.util.Map;

/**
 * single update
 */
public class SingleUpdateCmd implements MetaDataLikeCmd{

    private String boId;

    private Long id;

    private Map<String, Object> body;

    private String version;

    public SingleUpdateCmd(String boId, Long id, Map<String, Object> body, String version) {
        this.boId = boId;
        this.id = id;
        this.body = body;
        this.version = version;
    }

    @Override
    public String getBoId() {
        return boId;
    }

    @Override
    public String version() {
        return version;
    }

    public Long getId() {
        return id;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }
}
