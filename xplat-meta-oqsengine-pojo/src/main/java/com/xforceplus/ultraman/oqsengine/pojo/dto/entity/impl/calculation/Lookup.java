package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation.DEFAULT_LEVEL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.StringJoiner;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class Lookup extends AbstractCalculation {

    /**
     * lookup的类型标识.
     */
    @JsonProperty(value = "classId")
    private long classId;

    /**
     * lookup的字段标识.
     */
    @JsonProperty(value = "fieldId")
    private long fieldId;

    /**
     * lookup的关系信息.
     */
    @JsonProperty(value = "relationId")
    private long relationId;

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public long getRelationId() {
        return relationId;
    }

    public void setRelationId(long relationId) {
        this.relationId = relationId;
    }

    private Lookup() {
        super(CalculationType.LOOKUP);
    }

    @Override
    public AbstractCalculation clone() {
        Lookup lookup = new Lookup();
        lookup.classId = this.classId;
        lookup.fieldId = this.fieldId;
        lookup.relationId = this.relationId;
        lookup.level = this.level;
        return lookup;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Lookup.class.getSimpleName() + "[", "]")
            .add("classId=" + classId)
            .add("fieldId=" + fieldId)
            .add("relationId=" + relationId)
            .toString();
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long classId;
        private long fieldId;
        private long relationId;

        private Builder() {
        }

        public static Lookup.Builder anLookup() {
            return new Lookup.Builder();
        }

        public Lookup.Builder withClassId(long classId) {
            this.classId = classId;
            return this;
        }

        public Lookup.Builder withFieldId(long fieldId) {
            this.fieldId = fieldId;
            return this;
        }

        public Lookup.Builder withRelationId(long relationId) {
            this.relationId = relationId;
            return this;
        }

        /**
         * build.
         */
        public Lookup build() {
            Lookup lookup = new Lookup();
            lookup.calculationType = CalculationType.LOOKUP;
            lookup.classId = this.classId;
            lookup.fieldId = this.fieldId;
            lookup.relationId = this.relationId;
            lookup.level = DEFAULT_LEVEL;
            return lookup;
        }
    }
}
