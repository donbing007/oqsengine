package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 初始化参与者.
 */
public class InitCalculationAbstractParticipant extends AbstractParticipant implements Comparable<InitCalculationAbstractParticipant> {
    private IEntityClass sourceEntityClass;
    private List<IEntityField> sourceField;

    /**
     * 候选年龄，用于相同class内不同字段排序的依据.
     */
    private int age;

    /**
     * 正在初始化的实例.
     */
    private IEntity process;


    public IEntityClass getSourceEntityClass() {
        return sourceEntityClass;
    }

    /**
     * 获取源字段列表.
     */
    public List<IEntityField> getSourceField() {
        if (sourceField == null) {
            return Collections.emptyList();
        }
        return sourceField;
    }

    /**
     * 年龄加一.
     */
    public int growUp() {
        return ++age;
    }

    public int getAge() {
        return age;
    }

    public int getLevel() {
        return node.getLevel();
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
        final StringBuilder sb = new StringBuilder("Participant{");
        sb.append("sourceEntityClass").append(sourceEntityClass);
        sb.append("sourceField").append(sourceField);
        sb.append("entityClass=").append(super.getEntityClass());
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
        InitCalculationAbstractParticipant that = (InitCalculationAbstractParticipant) o;
        return Objects.equals(entityClass, that.entityClass) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, field);
    }

    @Override
    public int compareTo(InitCalculationAbstractParticipant participant) {
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
        private IEntityClass sourceEntityClass;
        private List<IEntityField> sourceField;
        private IEntityClass entityClass;
        private IEntityField field;
        private Collection<IEntity> affectedEntities;
        private Object attachment;
        private int age;
        private AbstractParticipant pre;

        private Builder() {
        }

        public static Builder anParticipant() {
            return new Builder();
        }

        public Builder withSourceEntityClass(IEntityClass sourceEntityClass) {
            this.sourceEntityClass = sourceEntityClass;
            return this;
        }



        public Builder withSourceField(List<IEntityField> sourceField) {
            this.sourceField = sourceField;
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

        public Builder withAge(int age) {
            this.age = age;
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

        public Builder withPre(AbstractParticipant pre) {
            this.pre = pre;
            return this;
        }

        /**
         * 构造.
         */
        public InitCalculationAbstractParticipant build() {
            InitCalculationAbstractParticipant participant = new InitCalculationAbstractParticipant();
            participant.sourceEntityClass = this.sourceEntityClass;
            participant.sourceField = this.sourceField;
            participant.affectedEntities = this.affectedEntities;
            participant.entityClass = this.entityClass;
            participant.attachment = this.attachment;
            participant.field = this.field;
            participant.age = this.age;
            participant.pre = this.pre;
            return participant;
        }
    }
}
