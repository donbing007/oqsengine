package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

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

    public InitCalculationParticipant() {
        super();
    }

    public IEntityClass getSourceEntityClass() {
        return sourceEntityClass;
    }

    public Collection<IEntityField> getSourceFields() {
        return this.sourceFields == null ? Collections.emptyList() : this.sourceFields;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InitCalculationParticipant that = (InitCalculationParticipant) o;
        return Objects.equals(getEntityClass(), that.getEntityClass()) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntityClass(), getField());
    }
}
