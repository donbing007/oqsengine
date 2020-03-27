package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

/**
 * local entity deleted event
 */
public class EntityDeleted implements EntityEvent {

    private String code;

    private Long id;

    private Map<String, String> context;

    public EntityDeleted(String code, Long id, Map<String, String> context) {
        this.code = code;
        this.id = id;
        this.context = context;
    }

    public String getCode() {
        return code;
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
