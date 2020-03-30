package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * a event will emit when entity class is updated
 */
public class EntityClassUpdated implements EntityClassEvent {

    private IEntityClass updatedClass;

    public EntityClassUpdated(IEntityClass updatedClass) {
        this.updatedClass = updatedClass;
    }

    public IEntityClass getUpdatedClass() {
        return updatedClass;
    }
}
