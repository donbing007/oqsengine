package com.xforceplus.ultraman.oqsengine.sdk.event;

import java.util.Map;

/**
 * entity exported
 */
public class EntityErrorExported implements EntityEvent{

    private Map<String, Object> context;

    private String fileName;

    private Long completedTime;

    private String reason;

    public EntityErrorExported(Map<String, Object> context, String fileName, String reason) {
        this.context = context;
        this.completedTime = System.currentTimeMillis();
        this.fileName = fileName;
        this.reason = reason;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Long getCompletedTime() {
        return completedTime;
    }

    public String getFileName() {
        return fileName;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "EntityErrorExported{" +
                "context=" + context +
                ", fileName='" + fileName + '\'' +
                ", completedTime=" + completedTime +
                ", reason='" + reason + '\'' +
                '}';
    }
}
