package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.util.Map;

/**
 * entity exported
 */
public class EntityExported implements EntityEvent{

    private Map<String, Object> context;

    private String downloadUrl;

    private String fileName;

    private String exportType;

    private Long completedTime;

    private String appId;

    private IEntityClass entityClass;

    public EntityExported(Map<String, Object> context, String downloadUrl, IEntityClass entityClass, String fileName, String exportType, String appId) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.completedTime = System.currentTimeMillis();
        this.fileName = fileName;
        this.exportType = exportType;
        this.appId = appId;
        this.entityClass = entityClass;
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

    public String getExportType() {
        return exportType;
    }

    public String getAppId() {
        return appId;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    @Override
    public String toString() {
        return "EntityExported{" +
                "context=" + context +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", exportType='" + exportType + '\'' +
                ", completedTime=" + completedTime +
                ", appId='" + appId + '\'' +
                '}';
    }
}
