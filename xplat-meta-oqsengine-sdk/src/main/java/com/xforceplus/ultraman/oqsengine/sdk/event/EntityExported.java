package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

/**
 * entity exported
 */
public class EntityExported implements EntityEvent{

    private Map<String, Object> context;

    private String downloadUrl;

    private String fileName;

    private Long completedTime;

    public EntityExported(Map<String, Object> context, String downloadUrl, String fileName) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.completedTime = System.currentTimeMillis();
        this.fileName = fileName;
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

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "EntityExported{" +
                "context=" + context +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", completedTime=" + completedTime +
                '}';
    }
}
