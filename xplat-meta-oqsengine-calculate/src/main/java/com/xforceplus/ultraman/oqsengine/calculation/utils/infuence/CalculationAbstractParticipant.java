package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * 计算字段影响树参与者.
 */
public class CalculationAbstractParticipant extends AbstractParticipant {

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Participant{");
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
        CalculationAbstractParticipant that = (CalculationAbstractParticipant) o;
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
        private IEntityClass entityClass;
        private IEntityField field;
        private Collection<IEntity> affectedEntities;
        private Object attachment;

        private Builder() {
        }

        public static Builder anParticipant() {
            return new Builder();
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
        public CalculationAbstractParticipant build() {
            CalculationAbstractParticipant participant = new CalculationAbstractParticipant();
            participant.affectedEntities = this.affectedEntities;
            participant.entityClass = this.entityClass;
            participant.attachment = this.attachment;
            participant.field = this.field;
            return participant;
        }
    }
}
