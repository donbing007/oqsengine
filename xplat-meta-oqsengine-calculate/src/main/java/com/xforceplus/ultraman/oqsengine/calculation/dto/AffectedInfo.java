package com.xforceplus.ultraman.oqsengine.calculation.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 表示影响信息,记录一个实例被另外一个实例影响的映射.
 *
 * @author dongbin
 * @version 0.1 2022/1/14 17:11
 * @since 1.8
 */
public class AffectedInfo {
    /**
     * 引起改变的实例标识.
     */
    private IEntity triggerEntity;

    /**
     * 被改变的实例标识.
     */
    private long affectedEntityId;

    public AffectedInfo(IEntity triggerEntity, long affectedEntityId) {
        this.triggerEntity = triggerEntity;
        this.affectedEntityId = affectedEntityId;
    }

    public IEntity getTriggerEntity() {
        return triggerEntity;
    }

    public long getAffectedEntityId() {
        return affectedEntityId;
    }
}
