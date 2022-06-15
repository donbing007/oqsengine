package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

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
    @JsonProperty(value = "aggregationConditions")
    private List<AggregationCondition> aggregationConditions;

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

    /**
     * 最终条件对象.
     */
    @JsonProperty(value = "conditions")
    private Conditions  conditions;

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


    public List<AggregationCondition> getAggregationConditions() {
        return aggregationConditions;
    }

    public void setAggregationConditions(
        List<AggregationCondition> aggregationConditions) {
        this.aggregationConditions = aggregationConditions;
    }

    public Map<Long, Long> getAggregationByFields() {
        return aggregationByFields;
    }

    public void setAggregationByFields(Map<Long, Long> aggregationByFields) {
        this.aggregationByFields = aggregationByFields;
    }

    public Optional<Conditions> getConditions() {
        return Optional.ofNullable(conditions);
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }


    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
            .add("classId=" + classId)
            .add("fieldId=" + fieldId)
            .add("relationId=" + relationId)
            .add("conditions=" + conditions)
            .add("aggregationType=" + aggregationType)
            .add("aggregationByFields=" + aggregationByFields)
            .toString();
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
        private List<AggregationCondition> aggregationConditions;
        private AggregationType aggregationType;
        private Map<Long, Long> aggregationByFields;

        private Builder() {
        }

        public static Aggregation.Builder anAggregation() {
            return new Aggregation.Builder();
        }

        public Aggregation.Builder withFieldId(long fieldId) {
            this.fieldId = fieldId;
            return this;
        }

        public Aggregation.Builder withClassId(long classId) {
            this.classId = classId;
            return this;
        }

        public Aggregation.Builder withRelationId(long relationId) {
            this.relationId = relationId;
            return this;
        }

        public Aggregation.Builder withConditions(Conditions conditions) {
            this.conditions = conditions;
            return this;
        }

        public Aggregation.Builder withAggregationConditions(List<AggregationCondition> aggregationConditions) {
            this.aggregationConditions = aggregationConditions;
            return this;
        }

        public Aggregation.Builder withAggregationType(AggregationType aggregationType) {
            this.aggregationType = aggregationType;
            return this;
        }

        public Aggregation.Builder withAggregationByFields(Map<Long, Long> aggregationByFields) {
            this.aggregationByFields = aggregationByFields;
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
            aggregation.aggregationConditions = this.aggregationConditions;

            return aggregation;
        }
    }

    /**
     * 聚合条件.
     */
    public static class AggregationCondition {
        private long entityClassId;
        private String entityClassCode;
        private String profile;
        private long entityFieldId;
        private String entityFieldCode;
        private FieldType fieldType;
        private ConditionOperator conditionOperator;
        private String stringValue;

        public long getEntityClassId() {
            return entityClassId;
        }

        public String getEntityClassCode() {
            return entityClassCode;
        }

        public String getProfile() {
            return profile;
        }

        public long getEntityFieldId() {
            return entityFieldId;
        }

        public String getEntityFieldCode() {
            return entityFieldCode;
        }

        public FieldType getFieldType() {
            return fieldType;
        }

        public ConditionOperator getConditionOperator() {
            return conditionOperator;
        }

        public String getStringValue() {
            return stringValue;
        }

        /**
         * 构造器.
         */
        public static final class Builder {
            private long entityClassId;
            private String entityClassCode;
            private String profile;
            private long entityFieldId;
            private String entityFieldCode;
            private FieldType fieldType;
            private ConditionOperator conditionOperator;
            private String stringValue;

            private Builder() {
            }

            public static AggregationCondition.Builder anAggregationCondition() {
                return new AggregationCondition.Builder();
            }

            public AggregationCondition.Builder withEntityClassId(long classId) {
                this.entityClassId = classId;
                return this;
            }

            public AggregationCondition.Builder withEntityClassCode(String entityClassCode) {
                this.entityClassCode = entityClassCode;
                return this;
            }

            public AggregationCondition.Builder withProfile(String profile) {
                this.profile = profile;
                return this;
            }

            public AggregationCondition.Builder withEntityFieldId(long entityFieldId) {
                this.entityFieldId = entityFieldId;
                return this;
            }

            public AggregationCondition.Builder withEntityFieldCode(String entityFieldCode) {
                this.entityFieldCode = entityFieldCode;
                return this;
            }

            public AggregationCondition.Builder withEntityFieldType(FieldType fieldType) {
                this.fieldType = fieldType;
                return this;
            }

            public AggregationCondition.Builder withConditionOperator(ConditionOperator conditionOperator) {
                this.conditionOperator = conditionOperator;
                return this;
            }

            public AggregationCondition.Builder withStringValue(String stringValue) {
                this.stringValue = stringValue;
                return this;
            }

            /**
             * build.
             */
            public AggregationCondition build() {
                AggregationCondition aggregationCondition = new AggregationCondition();
                aggregationCondition.entityClassId = this.entityClassId;
                aggregationCondition.entityClassCode = this.entityClassCode;
                aggregationCondition.entityFieldId = this.entityFieldId;
                aggregationCondition.entityFieldCode = this.entityFieldCode;
                aggregationCondition.conditionOperator = this.conditionOperator;
                aggregationCondition.fieldType = this.fieldType;
                aggregationCondition.profile = this.profile;
                aggregationCondition.stringValue = this.stringValue;

                return aggregationCondition;
            }
        }
    }
}
