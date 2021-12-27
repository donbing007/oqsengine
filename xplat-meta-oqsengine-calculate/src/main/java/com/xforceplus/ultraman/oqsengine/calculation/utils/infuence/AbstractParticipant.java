package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 参与者抽像.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 12:07
 * @since 1.8
 */
public abstract class AbstractParticipant implements Participant {

    private String id;
    private IEntityClass entityClass;
    private IEntityField field;
    private Collection<IEntity> affectedEntities;
    private Object attachment;

    public AbstractParticipant() {
        this.id = UUID.randomUUID().toString();
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public void setField(IEntityField field) {
        this.field = field;
    }

    public void setAffectedEntities(
        Collection<IEntity> affectedEntities) {
        this.affectedEntities = affectedEntities;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IEntityClass getEntityClass() {
        return this.entityClass;
    }

    @Override
    public IEntityField getField() {
        return this.field;
    }

    @Override
    public Collection<IEntity> getAffectedEntities() {
        if (this.affectedEntities == null) {
            return Collections.emptyList();
        }

        return affectedEntities;
    }

    @Override
    public void addAffectedEntity(IEntity entity) {
        if (this.affectedEntities == null) {
            this.affectedEntities = new LinkedList<>();
        }

        this.affectedEntities.add(entity);
    }

    @Override
    public Optional<IEntity> removeAffectedEntities(long id) {
        if (affectedEntities == null) {
            return Optional.empty();
        } else {
            AtomicReference<IEntity> targetEntity = new AtomicReference<>();
            this.affectedEntities.removeIf(e -> {
                if (e.id() == id) {
                    targetEntity.set(e);
                    return true;
                } else {
                    return false;
                }
            });

            return Optional.ofNullable(targetEntity.get());
        }
    }

    @Override
    public Optional<Object> getAttachment() {
        return Optional.ofNullable(this.attachment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractParticipant{");
        sb.append("entityClass=").append(entityClass);
        sb.append(", field=").append(field);
        sb.append(", affectedEntities=").append(affectedEntities);
        sb.append(", attachment=").append(attachment);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractParticipant)) {
            return false;
        }
        AbstractParticipant that = (AbstractParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
