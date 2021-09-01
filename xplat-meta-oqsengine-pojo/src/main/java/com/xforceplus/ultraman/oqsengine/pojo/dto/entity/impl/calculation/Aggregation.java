package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation.DEFAULT_LEVEL;

/**
 * 聚合字段属性.
 *
 * @className: AggregationTaskBuilderUtils
 * @author: wangzheng
 * @date: 2021/8/31 16:24
 */
public class Aggregation extends AbstractCalculation {

    /**
     * 聚合对象标识.
     */
    @JsonProperty(value = "classId")
    private long classId;

    /**
     * 聚合对象指定字段标识.
     */
    @JsonProperty(value = "fieldId")
    private long fieldId;

    /**
     * 聚合对象关系标识.
     */
    @JsonProperty(value = "relationId")
    private long relationId;

    /**
     * 聚合字段条件信息.
     */
    @JsonProperty(value = "condition")
    private String condition;

    /**
     * 被聚合信息Map fieldId,entityClassId.
     */
    @JsonProperty(value = "condition")
    private Map<Long,Long> aggregationByFields;

    public Aggregation(CalculationType calculationType) {
        super(calculationType);
    }

    public Aggregation() {
        super(CalculationType.AGGREGATION);
    }

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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Map<Long, Long> getAggregationByFields() {
        return aggregationByFields;
    }

    public void setAggregationByFields(Map<Long, Long> aggregationByFields) {
        this.aggregationByFields = aggregationByFields;
    }

    @Override
    public AbstractCalculation clone() {
        Aggregation aggregation = new Aggregation();
        aggregation.classId = this.classId;
        aggregation.fieldId = this.fieldId;
        aggregation.level = this.level;
        aggregation.aggregationByFields = this.aggregationByFields;
        aggregation.condition = this.condition;
        aggregation.relationId = this.relationId;
        return aggregation;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long classId;
        private long fieldId;
        private long relationId;
        private String condition;
        private Map<Long, Long> aggregationByFields;

        private Builder() {
        }

        public static Aggregation.Builder anAggregation() {
            return new Aggregation.Builder();
        }

        public Aggregation.Builder withClassId(long classId) {
            this.classId = classId;
            return this;
        }

        public Aggregation.Builder withFieldId(long fieldId) {
            this.fieldId = fieldId;
            return this;
        }

        /**
         * build.
         */
        public Aggregation build() {
            Aggregation aggregation = new Aggregation();
            aggregation.calculationType = CalculationType.AGGREGATION;
            aggregation.classId = this.classId;
            aggregation.fieldId = this.fieldId;
            aggregation.level = DEFAULT_LEVEL;
            aggregation.aggregationByFields = this.aggregationByFields;
            aggregation.condition = this.condition;
            aggregation.relationId = this.relationId;
            return aggregation;
        }
    }
}
