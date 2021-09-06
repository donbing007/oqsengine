package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.Map;

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
    @JsonProperty(value = "conditions")
    private Conditions conditions;

    /**
     * 聚合字段类型.
     */
    @JsonProperty(value = "aggregationType")
    private AggregationType aggregationType = AggregationType.UNKNOWN;

    /**
     * 被聚合信息Map fieldId,entityClassId.
     */
    @JsonProperty(value = "condition")
    private Map<Long, Long> aggregationByFields;

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

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public Map<Long, Long> getAggregationByFields() {
        return aggregationByFields;
    }

    public void setAggregationByFields(Map<Long, Long> aggregationByFields) {
        this.aggregationByFields = aggregationByFields;
    }

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    @Override
    public AbstractCalculation clone() {
        Aggregation aggregation = new Aggregation();
        aggregation.classId = this.classId;
        aggregation.fieldId = this.fieldId;
        aggregation.level = this.level;
        aggregation.aggregationByFields = this.aggregationByFields;
        aggregation.conditions = this.conditions;
        aggregation.relationId = this.relationId;
        aggregation.aggregationType = this.aggregationType;
        return aggregation;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long classId;
        private long fieldId;
        private long relationId;
        private Conditions conditions;
        private AggregationType aggregationType;
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
            aggregation.level = StaticCalculation.DEFAULT_LEVEL;
            aggregation.aggregationByFields = this.aggregationByFields;
            aggregation.conditions = this.conditions;
            aggregation.relationId = this.relationId;
            aggregation.aggregationType = this.aggregationType;
            return aggregation;
        }
    }
}
