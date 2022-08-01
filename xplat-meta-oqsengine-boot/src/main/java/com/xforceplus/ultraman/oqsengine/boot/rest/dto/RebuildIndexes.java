package com.xforceplus.ultraman.oqsengine.boot.rest.dto;

import java.util.List;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
public class RebuildIndexes {
    private List<String> entityClassIds;
    private String appId;
    private String start;
    private String end;

    public RebuildIndexes() {
    }

    public List<String> getEntityClassIds() {
        return entityClassIds;
    }

    public void setEntityClassIds(List<String> entityClassIds) {
        this.entityClassIds = entityClassIds;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
