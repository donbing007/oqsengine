package com.xforceplus.ultraman.oqsengine.sdk.command;

import java.util.Map;

/**
 * single update
 */
public class SingleUpdateCmd implements MetaDataLikeCmd{

    private String boId;

    private Long id;

    private Map<String, Object> body;

    public SingleUpdateCmd(String boId, Long id, Map<String, Object> body) {
        this.boId = boId;
        this.id = id;
        this.body = body;
    }

    public String getBoId() {
        return boId;
    }

    public Long getId() {
        return id;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
