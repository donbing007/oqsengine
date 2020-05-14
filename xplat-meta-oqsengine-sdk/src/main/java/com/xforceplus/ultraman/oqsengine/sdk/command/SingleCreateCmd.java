package com.xforceplus.ultraman.oqsengine.sdk.command;

import java.util.Map;


/**
 * create a single record
 */
public class SingleCreateCmd implements MetaDataLikeCmd{

    private String boId;
    private Map<String, Object> body;
    private String version;

    public SingleCreateCmd(String boId, Map<String, Object> body, String version) {
        this.boId = boId;
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

    @Override
    public void clearVersion() {
        this.version = null;
    }

    public Map<String, Object> getBody() {
        return body;
    }

}
