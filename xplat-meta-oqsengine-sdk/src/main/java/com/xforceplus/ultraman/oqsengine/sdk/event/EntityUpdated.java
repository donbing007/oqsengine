package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

public class EntityUpdated implements EntityEvent{

    private String code;

    private Long id;

    private Map<String, Object> data;

    private Map<String, String> context;

    public EntityUpdated(String code, Long id, Map<String, Object> data, Map<String, String> context) {
        this.code = code;
        this.id = id;
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
}
