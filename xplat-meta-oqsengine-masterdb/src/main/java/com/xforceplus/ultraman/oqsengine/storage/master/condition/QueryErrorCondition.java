package com.xforceplus.ultraman.oqsengine.storage.master.condition;

import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class QueryErrorCondition {
    Long maintainId;
    Long id;
    Long entity;
    Long startTime;
    Long endTime;
    FixedStatus fixedStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntity() {
        return entity;
    }

    public void setEntity(Long entity) {
        this.entity = entity;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public FixedStatus getFixedStatus() {
        return fixedStatus;
    }

    public void setFixedStatus(FixedStatus fixedStatus) {
        this.fixedStatus = fixedStatus;
    }

    public Long getMaintainId() {
        return maintainId;
    }

    public void setMaintainId(Long maintainId) {
        this.maintainId = maintainId;
    }
}
