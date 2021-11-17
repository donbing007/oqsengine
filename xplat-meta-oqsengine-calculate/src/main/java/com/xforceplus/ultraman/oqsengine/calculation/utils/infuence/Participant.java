package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Optional;

/**
 * 影响树参与者.
 */
public interface Participant {
    public IEntityClass getEntityClass();

    public IEntityField getField();

    public Collection<IEntity> getAffectedEntities();

    public void addAffectedEntity(IEntity entity);

    public Optional<IEntity> removeAffectedEntities(long id);

    public Optional<Object> getAttachment();

}
