package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 初始化参与者.
 */
public class InitCalculationParticipant implements Participant {
    private IEntityClass sourceEntityClass;
    private IEntityField sourceField;
    private IEntityClass entityClass;
    private IEntityField field;
    private Collection<IEntity> affectedEntities;
    private Object attachment;


    public IEntityClass getSourceEntityClass() {
        return sourceEntityClass;
    }

    public IEntityField getSourceField() {
        return sourceField;
    }

    @Override
    public IEntityClass getEntityClass() {
        return entityClass;
    }

    @Override
    public IEntityField getField() {
        return field;
    }

    /**
     * 获得得当前参与者的影响entity列表.
     *
     * @return 影响entity列表.
     */
    @Override
    public Collection<IEntity> getAffectedEntities() {
        if (affectedEntities == null) {
            return Collections.emptyList();
        } else {
            return affectedEntities;
        }
    }

    /**
     * 增加受影响的实例.
     *
     * @param entity 实例.
     */
    @Override
    public void addAffectedEntity(IEntity entity) {
        if (affectedEntities == null) {
            this.affectedEntities = new ArrayList<>();
        }

        this.affectedEntities.add(entity);
    }

    /**
     * 删除受影响的实例.
     *
     * @param id 实例标识.
     * @return 删除的实例.
     */
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
        return Optional.ofNullable(attachment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Participant{");
        sb.append("sourceEntityClass").append(sourceEntityClass);
        sb.append("sourceField").append(sourceField);
        sb.append("entityClass=").append(entityClass);
        sb.append(", field=").append(field);
        sb.append(", attachment=").append(attachment);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InitCalculationParticipant that = (InitCalculationParticipant) o;
        return Objects.equals(entityClass, that.entityClass) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, field);
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private IEntityClass sourceEntityClass;
        private IEntityField sourceField;
        private IEntityClass entityClass;
        private IEntityField field;
        private Collection<IEntity> affectedEntities;
        private Object attachment;

        private Builder() {
        }

        public static Builder anParticipant() {
            return new Builder();
        }

        public Builder withSourceEntityClass(IEntityClass sourceEntityClass) {
            this.sourceEntityClass = entityClass;
            return this;
        }

        public Builder withSourceField(IEntityField sourceField) {
            this.sourceField = field;
            return this;
        }


        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withField(IEntityField field) {
            this.field = field;
            return this;
        }

        public Builder withAffectedEntities(Collection<IEntity> affectedEntities) {
            this.affectedEntities = new ArrayList<>(affectedEntities);
            return this;
        }

        public Builder withAttachment(Object attachment) {
            this.attachment = attachment;
            return this;
        }

        /**
         * 构造.
         */
        public InitCalculationParticipant build() {
            InitCalculationParticipant participant = new InitCalculationParticipant();
            participant.sourceEntityClass = this.sourceEntityClass;
            participant.sourceField = this.sourceField;
            participant.affectedEntities = this.affectedEntities;
            participant.entityClass = this.entityClass;
            participant.attachment = this.attachment;
            participant.field = this.field;
            return participant;
        }
    }
}
