package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 字段配置信息.
 *
 * @author dongbin
 * @version 0.1 2020/2/26 14:32
 * @since 1.8
 */
public class FieldConfig implements Serializable {

    /**
     * 字段意义.
     */
    public enum FieldSense {
        UNKNOWN(0),
        /**
         * 普通属性.
         */
        NORMAL(1),
        TENANT_ID(2),
        TENANT_CODE(3),
        CREATE_TIME(4),
        UPDATE_TIME(5),
        CREATE_USER_ID(6),
        UPDATE_USER_ID(7),
        CREATE_USER_NAME(8),
        UPDATE_USER_NAME(9),
        DELETE_FLAG(10);

        private int symbol;

        private FieldSense(int symbol) {
            this.symbol = symbol;
        }

        public int getSymbol() {
            return symbol;
        }

        public static FieldSense getInstance(int symbol) {
            for (FieldSense sense : FieldSense.values()) {
                if (sense.getSymbol() == symbol) {
                    return sense;
                }
            }

            return null;
        }
    }

    /**
     * 模糊类型.
     * 数字只允许NOT.
     */
    public enum FuzzyType {
        UNKNOWN(0),
        /**
         * 不作处理
         */
        NOT(1),
        /**
         * 通配符
         */
        WILDCARD(2),
        /**
         * 分词
         */
        SEGMENTATION(3);

        private int symbol;

        private FuzzyType(int symbol) {
            this.symbol = symbol;
        }

        public int getSymbol() {
            return symbol;
        }

        public static FuzzyType getInstance(int symbol) {
            for (FuzzyType type : FuzzyType.values()) {
                if (type.getSymbol() == symbol) {
                    return type;
                }
            }

            return null;
        }
    }

    /**
     * 是否可搜索.true 可搜索,false 不可搜索.
     */
    @JsonProperty(value = "searchable")
    private boolean searchable = false;

    /**
     * 字符串:     最大字符个数.
     * 数字:       最大的数字.
     * 日期/时间:   离现在最近的时间日期.
     */
    @JsonProperty(value = "max")
    private long max = Long.MAX_VALUE;

    /**
     * 字符串:     最小字符个数.
     * 数字:       最小的数字.
     * 日期/时间:   离现在最远的时间日期.
     */
    @JsonProperty(value = "min")
    private long min = Long.MIN_VALUE;

    /**
     * 字段精度
     */
    @JsonProperty(value = "precision")
    private int precision = 0;

    /**
     * 是否为数据标识.
     */
    @JsonProperty(value = "identifie")
    private boolean identifie = false;

    /**
     * 是否必填字段.
     */
    @JsonProperty(value = "required")
    private boolean required = false;

    /**
     * 字段意义.
     */
    @JsonProperty(value = "fieldSense")
    private FieldSense fieldSense = FieldSense.NORMAL;

    /**
     * 校验正则.
     */
    @JsonProperty(value = "validateRegexString")
    private String validateRegexString = "";

    @JsonProperty(value = "splittable")
    private boolean splittable = false;

    @JsonProperty(value = "delimiter")
    private String delimiter = "";

    @JsonProperty(value = "displayType")
    private String displayType = "";

    @JsonProperty(value = "fuzzyType")
    private FuzzyType fuzzyType = FuzzyType.NOT;

    /**
     * 创建一个新的 FieldConfig.
     *
     * @return 实例.
     */
    public static FieldConfig build() {
        return new FieldConfig();
    }

    public FieldConfig precision(int precision) {
        this.precision = precision;
        return this;
    }

    /**
     * 设置是否可搜索,默认不搜索.
     *
     * @param searchable true 可搜索, false 不可搜索.
     * @return 当前实例.
     */
    public FieldConfig searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    /**
     * 字符串:     最大字符个数.
     * 数字:       最大的数字.
     * 日期/时间:   离现在最近的时间日期.
     *
     * @param max 值.
     * @return 当前实例.
     */
    public FieldConfig max(long max) {
        this.max = max;
        return this;
    }

    /**
     * 字符串:     最小字符个数.
     * 数字:       最小的数字.
     * 日期/时间:   离现在最远的时间日期.
     *
     * @param min 值.
     * @return 当前实例.
     */
    public FieldConfig min(long min) {
        this.min = min;
        return this;
    }

    public FieldConfig identifie(boolean identifie) {
        this.identifie = identifie;
        return this;
    }

    /**
     * 模糊类型.
     *
     * @param type
     * @return
     */
    public FieldConfig fuzzyType(FuzzyType type) {
        this.fuzzyType = type;
        return this;
    }

    /**
     * 是否表示一个数据标识.
     *
     * @return true 数据标识,false 非数据标识.
     */
    public boolean isIdentifie() {
        return identifie;
    }

    /**
     * 是否可搜索.true 可搜索,false 不可搜索.
     *
     * @return 结果.
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * 获取最大值.
     *
     * @return 最大值.
     */
    public long getMax() {
        return max;
    }

    /**
     * 获取最小值.
     *
     * @return
     */
    public long getMin() {
        return min;
    }

    public int precision() {
        return precision;
    }

    public String getDisplayType() {
        return this.displayType;
    }

    public FieldConfig displayType(String displayType) {
        this.displayType = displayType;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public FieldConfig required(boolean required) {
        this.required = required;
        return this;
    }

    public FieldSense getFieldSense() {
        return fieldSense;
    }

    public FieldConfig fieldSense(FieldSense fieldSense) {
        this.fieldSense = fieldSense;
        return this;
    }

    public String getValidateRegexString() {
        return validateRegexString;
    }

    public FieldConfig validateRegexString(String validateRegexString) {
        this.validateRegexString = validateRegexString;
        return this;
    }

    public boolean isSplittable() {
        return splittable;
    }

    public FieldConfig splittable(boolean splittable) {

        this.splittable = splittable;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public FieldConfig delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public FuzzyType getFuzzyType() {
        return fuzzyType;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldConfig)) {
            return false;
        }
        FieldConfig that = (FieldConfig) o;
        return isSearchable() == that.isSearchable() &&
            getMax() == that.getMax() &&
            getMin() == that.getMin() &&
            precision() == that.precision() &&
            isIdentifie() == that.isIdentifie() &&
            isRequired() == that.isRequired() &&
            isSplittable() == that.isSplittable() &&
            getFieldSense() == that.getFieldSense() &&
            Objects.equals(getValidateRegexString(), that.getValidateRegexString()) &&
            Objects.equals(getDelimiter(), that.getDelimiter()) &&
            Objects.equals(getDisplayType(), that.getDisplayType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            isSearchable(),
            getMax(),
            getMin(),
            precision(),
            isIdentifie(),
            isRequired(),
            getFieldSense(),
            getValidateRegexString(),
            isSplittable(),
            getDelimiter(),
            getDisplayType());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FieldConfig{");
        sb.append("searchable=").append(searchable);
        sb.append(", max=").append(max);
        sb.append(", min=").append(min);
        sb.append(", precision=").append(precision);
        sb.append(", identifie=").append(identifie);
        sb.append(", required=").append(required);
        sb.append(", fieldSense=").append(fieldSense);
        sb.append(", validateRegexString='").append(validateRegexString).append('\'');
        sb.append(", splittable=").append(splittable);
        sb.append(", delimiter='").append(delimiter).append('\'');
        sb.append(", displayType='").append(displayType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        private boolean searchable = false;
        private long max = Long.MAX_VALUE;
        private long min = Long.MIN_VALUE;
        private int precision = 0;
        private boolean identifie = false;
        private boolean required = false;
        private FieldSense fieldSense = FieldSense.NORMAL;
        private String validateRegexString = "";
        private boolean splittable = false;
        private String delimiter = "";
        private String displayType = "";
        private FuzzyType fuzzyType = FuzzyType.NOT;

        private Builder() {
        }

        public static Builder aFieldConfig() {
            return new Builder();
        }

        public Builder withSearchable(boolean searchable) {
            this.searchable = searchable;
            return this;
        }

        public Builder withMax(long max) {
            this.max = max;
            return this;
        }

        public Builder withMin(long min) {
            this.min = min;
            return this;
        }

        public Builder withPrecision(int precision) {
            this.precision = precision;
            return this;
        }

        public Builder withIdentifie(boolean identifie) {
            this.identifie = identifie;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withFieldSense(FieldSense fieldSense) {
            this.fieldSense = fieldSense;
            return this;
        }

        public Builder withValidateRegexString(String validateRegexString) {
            this.validateRegexString = validateRegexString;
            return this;
        }

        public Builder withSplittable(boolean splittable) {
            this.splittable = splittable;
            return this;
        }

        public Builder withDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder withDisplayType(String displayType) {
            this.displayType = displayType;
            return this;
        }

        public Builder withFuzzyType(FuzzyType fuzzyType) {
            this.fuzzyType = fuzzyType;
            return this;
        }

        public FieldConfig build() {
            FieldConfig fieldConfig = new FieldConfig();
            fieldConfig.max = this.max;
            fieldConfig.required = this.required;
            fieldConfig.identifie = this.identifie;
            fieldConfig.splittable = this.splittable;
            fieldConfig.fuzzyType = this.fuzzyType;
            fieldConfig.validateRegexString = this.validateRegexString;
            fieldConfig.min = this.min;
            fieldConfig.searchable = this.searchable;
            fieldConfig.fieldSense = this.fieldSense;
            fieldConfig.precision = this.precision;
            fieldConfig.delimiter = this.delimiter;
            fieldConfig.displayType = this.displayType;
            return fieldConfig;
        }
    }
}
