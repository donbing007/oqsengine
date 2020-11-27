package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * desc :
 * name : BatchCondition
 *
 * @author : xujia
 * date : 2020/8/22
 * @since : 1.8
 */
public class BatchCondition {
    private long startTime;
    private long endTime;
    private IEntityClass entityClass;

    public BatchCondition(long startTime, long endTime, IEntityClass entityClass) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.entityClass = entityClass;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }
}
