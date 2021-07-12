package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

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

    private Lookup() {
        super(CalculationType.LOOKUP);
    }

    @Override
    public AbstractCalculation clone() {
        Lookup lookup = new Lookup();
        lookup.classId = this.classId;
        lookup.fieldId = this.fieldId;
        return lookup;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long classId;
        private long fieldId;

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

        /**
         * build.
         */
        public Lookup build() {
            Lookup lookup = new Lookup();
            lookup.calculationType = CalculationType.LOOKUP;
            lookup.classId = this.classId;
            lookup.fieldId = this.fieldId;

            return lookup;
        }
    }
}
