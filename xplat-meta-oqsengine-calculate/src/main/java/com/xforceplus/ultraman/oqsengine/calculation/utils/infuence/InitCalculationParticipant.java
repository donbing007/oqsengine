package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Collections;

/**
 * 初始化参与者.
 */
public class InitCalculationParticipant extends AbstractParticipant implements Comparable<InitCalculationParticipant> {

    private IEntityClass sourceEntityClass;
    private Collection<IEntityField> sourceFields;

    /**
     * 候选年龄，用于相同class内不同字段排序的依据.
     */
    private int age;

    /**
     * 正在初始化的实例.
     */
    private IEntity process;

    private boolean needInit;

    public InitCalculationParticipant() {
        super();
    }

    public IEntityClass getSourceEntityClass() {
        return sourceEntityClass;
    }

    public Collection<IEntityField> getSourceFields() {
        return this.sourceFields == null ? Collections.emptyList() : this.sourceFields;
    }

    public boolean isNeedInit() {
        return needInit;
    }

    public int growUp() {
        return ++age;
    }

    public int getAge() {
        return age;
    }

    public IEntity getProcess() {
        return process;
    }

    public void setProcess(IEntity process) {
        this.process = process;
    }


    /**
     * entity实例是否变化.
     */
    public boolean isChange(IEntity entity) {
        if (this.process == null) {
            return false;
        }
        return this.process.equals(entity);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InitCalculationParticipant{");
        sb.append("sourceEntityClass=").append(sourceEntityClass);
        sb.append(", sourceField=").append(sourceFields);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(InitCalculationParticipant participant) {
        if (this.age - participant.age == 0) {
            return this.getField().compareTo(participant.getField());
        } else {
            // 降序.
            return participant.age - this.age;
        }
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private IEntityClass entityClass;
        private IEntityField field;
        private Collection<IEntity> affectedEntities;
        private Object attachment;
        private IEntityClass sourceEntityClass;
        private Collection<IEntityField> sourceFields;
        private int age;
        private IEntity process;

        private boolean needInit;

        private Builder() {}

        public static Builder anInitCalculationParticipant() {
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

        public Builder withSourceEntityClass(IEntityClass sourceEntityClass) {
            this.sourceEntityClass = sourceEntityClass;
            return this;
        }

        public Builder withSourceFields(Collection<IEntityField> sourceFields) {
            this.sourceFields = sourceFields;
            return this;
        }

        public Builder withAge(int age) {
            this.age = age;
            return this;
        }

        public Builder withProcess(IEntity process) {
            this.process = process;
            return this;
        }

        public Builder withNeedInit(boolean need) {
            this.needInit = need;
            return this;
        }

        /**
         * 构造.
         */
        public InitCalculationParticipant build() {
            InitCalculationParticipant initCalculationParticipant = new InitCalculationParticipant();
            initCalculationParticipant.setEntityClass(entityClass);
            initCalculationParticipant.setField(field);
            initCalculationParticipant.setAffectedEntities(affectedEntities);
            initCalculationParticipant.setAttachment(attachment);
            initCalculationParticipant.setProcess(process);
            initCalculationParticipant.sourceEntityClass = this.sourceEntityClass;
            initCalculationParticipant.age = this.age;
            initCalculationParticipant.sourceFields = this.sourceFields;
            initCalculationParticipant.needInit = this.needInit;
            return initCalculationParticipant;
        }
    }
}
