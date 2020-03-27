package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

/**
 * local entity created event
 */
public class EntityCreated implements EntityEvent {

    private String code;

    private String childCode;

    private Long id;

    private Long childId;

    private Map<String, Object> data;

    private Map<String, String> context;

    public EntityCreated(String code, Long id, Map<String, Object> data, Map<String, String> context) {
        this.code = code;
        this.id = id;
        this.data = data;
        this.context = context;
    }

    public EntityCreated(String code, String childCode, Long id, Long childId, Map<String, Object> data, Map<String, String> context) {
        this.code = code;
        this.childCode = childCode;
        this.id = id;
        this.childId = childId;
        this.data = data;
        this.context = context;
    }

    public String getCode() {
        return code;
    }

    public Long getId() {
        return id;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildCode() {
        return childCode;
    }
}
