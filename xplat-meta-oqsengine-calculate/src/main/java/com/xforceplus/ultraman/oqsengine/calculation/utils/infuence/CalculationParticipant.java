package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;

/**
 * 计算字段影响树参与者.
 */
public class CalculationParticipant extends AbstractParticipant {

    public CalculationParticipant() {
        super();
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private IEntityClass entityClass;
        private IEntityField field;
        private Collection<IEntity> affectedEntities;
        private Object attachment;

        private Builder() {}

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
            this.affectedEntities = affectedEntities;
            return this;
        }

        public Builder withAttachment(Object attachment) {
            this.attachment = attachment;
            return this;
        }

        /**
         * 构造.
         *
         * @return 实例.
         */
        public CalculationParticipant build() {
            CalculationParticipant calculationParticipant = new CalculationParticipant();
            calculationParticipant.setEntityClass(entityClass);
            calculationParticipant.setField(field);
            calculationParticipant.setAffectedEntities(affectedEntities);
            calculationParticipant.setAttachment(attachment);
            return calculationParticipant;
        }
    }
}
