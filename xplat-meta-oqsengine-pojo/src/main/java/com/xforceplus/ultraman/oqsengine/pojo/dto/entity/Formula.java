package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import static com.xforceplus.ultraman.oqsengine.formula.client.dto.ExpressionWrapper.Builder.DEFAULT_FORMULA_LEVEL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.formula.client.utils.MD5Utils;

/**
 * Created by justin.xu on 05/2021
 */
public class Formula {

    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "formula")
    private String formula;

    @JsonProperty(value = "level")
    private int level;

    @JsonProperty(value = "validator")
    private String validator;

    @JsonProperty(value = "min")
    private String min;

    @JsonProperty(value = "max")
    private String max;

    @JsonProperty(value = "condition")
    private String condition;

    @JsonProperty(value = "emptyValueTransfer")
    private String emptyValueTransfer;

    public String getCode() {
        return code;
    }

    public String getFormula() {
        return formula;
    }

    public String getValidator() {
        return validator;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public String getCondition() {
        return condition;
    }

    public String getEmptyValueTransfer() {
        return emptyValueTransfer;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setEmptyValueTransfer(String emptyValueTransfer) {
        this.emptyValueTransfer = emptyValueTransfer;
    }

    /**
     * builder.
     */
    public static final class Builder {

        private String formula;
        private Integer level = DEFAULT_FORMULA_LEVEL;
        private String validator;
        private String min;
        private String max;
        private String condition;
        private String emptyValueTransfer;

        private Builder() {
        }

        public static Formula.Builder anFormula() {
            return new Formula.Builder();
        }

        public Formula.Builder withFormula(String formula) {
            this.formula = formula;
            return this;
        }

        public Formula.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public Formula.Builder withValidator(String validator) {
            this.validator = validator;
            return this;
        }

        public Formula.Builder withMin(String min) {
            this.min = min;
            return this;
        }

        public Formula.Builder withMax(String max) {
            this.max = max;
            return this;
        }

        public Formula.Builder withCondition(String condition) {
            this.condition = condition;
            return this;
        }

        public Formula.Builder withEmptyValueTransfer(String emptyValueTransfer) {
            this.emptyValueTransfer = emptyValueTransfer;
            return this;
        }

        /**
         * build.
         */
        public Formula build() {
            Formula formula = new Formula();
            if (null != this.formula && !this.formula.isEmpty()) {
                formula.formula = this.formula;
                formula.code = MD5Utils.encrypt(this.formula);
            }
            formula.level = this.level;
            formula.validator = this.validator;
            formula.min = this.min;
            formula.max = this.max;
            formula.condition = this.condition;
            formula.emptyValueTransfer = this.emptyValueTransfer;

            return formula;
        }
    }
}
