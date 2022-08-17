package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 参与者抽像.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 12:07
 * @since 1.8
 */
public abstract class AbstractParticipant implements Participant {

    private IEntityClass entityClass;
    private IEntityField field;
    private Collection<IEntity> affectedEntities;
    private Object attachment;

    private boolean source;

    private boolean needless;

    @Override
    public String getId() {
        StringBuilder buff = new StringBuilder();
        buff.append(source);
        buff.append(getEntityClass() == null ? 0 : getEntityClass().id())
            .append(".")
            .append(getField() == null ? 0 : getField().id());
        return buff.toString();
    }

    protected void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    protected void setField(IEntityField field) {
        this.field = field;
    }

    protected void setAffectedEntities(Collection<IEntity> affectedEntities) {
        if (affectedEntities != null) {
            this.affectedEntities = new LinkedList<>(affectedEntities);
        }
    }

    public void source() {
        this.source = true;
    }

    @Override
    public boolean isNeedless() {
        return this.needless;
    }

    @Override
    public void needless() {
        this.needless = true;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
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
    public boolean isSource() {
        return source;
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
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" (")
            .append(this.getEntityClass().code())
            .append(",")
            .append(this.getField().name())
            .append(")");

        return sb.toString();
    }


}
