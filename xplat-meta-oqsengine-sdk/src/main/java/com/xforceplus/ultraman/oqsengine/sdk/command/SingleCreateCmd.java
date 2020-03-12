package com.xforceplus.ultraman.oqsengine.sdk.command;

import java.util.Map;

public class SingleCreateCmd implements MetaDataLikeCmd{

    private String boId;
    private Map<String, Object> body;

    public SingleCreateCmd(String boId, Map<String, Object> body) {
        this.boId = boId;
        this.body = body;
    }

    public String getBoId() {
        return boId;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
