package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

/**
 * entity exported
 */
public class EntityExported implements EntityEvent{

    private Map<String, Object> context;

    private String downloadUrl;

    private Long completedTime;

    public EntityExported(Map<String, Object> context, String downloadUrl) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.completedTime = System.currentTimeMillis();
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Long getCompletedTime() {
        return completedTime;
    }
}
