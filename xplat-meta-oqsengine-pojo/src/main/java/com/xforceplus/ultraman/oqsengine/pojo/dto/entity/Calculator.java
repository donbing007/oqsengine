package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.utils.MD5Utils;

/**
 * Created by justin.xu on 05/2021
 */
public class Calculator {
    @JsonProperty(value = "calculateType")
    private CalculateType calculateType;

    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "expression")
    private String expression;

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

    @JsonProperty(value = "patten")
    private String patten;

    @JsonProperty(value = "model")
    private String model;

    @JsonProperty(value = "step")
    private int step;

    @JsonProperty(value = "level")
    private int level;


    public String getCode() {
        return code;
    }

    public String getExpression() {
        return expression;
    }

    public CalculateType getCalculateType() {
        return calculateType;
    }

    public void setCalculateType(CalculateType calculateType) {
        this.calculateType = calculateType;
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

    public void setExpression(String expression) {
        this.expression = expression;
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

    public String getPatten() {
        return patten;
    }

    public void setPatten(String patten) {
        this.patten = patten;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private CalculateType calculateType;
        private String expression;
        private Integer level = ExecutionWrapper.Builder.DEFAULT_FORMULA_LEVEL;
        private String validator;
        private String min;
        private String max;
        private String condition;
        private String emptyValueTransfer;
        private String patten;
        private String model;
        private int step;

        private Builder() {
        }

        public static Calculator.Builder anCalculator() {
            return new Calculator.Builder();
        }

        public Calculator.Builder withCalculateType(CalculateType calculateType) {
            this.calculateType = calculateType;
            return this;
        }

        public Calculator.Builder withExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public Calculator.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public Calculator.Builder withValidator(String validator) {
            this.validator = validator;
            return this;
        }

        public Calculator.Builder withMin(String min) {
            this.min = min;
            return this;
        }

        public Calculator.Builder withMax(String max) {
            this.max = max;
            return this;
        }

        public Calculator.Builder withCondition(String condition) {
            this.condition = condition;
            return this;
        }

        public Calculator.Builder withEmptyValueTransfer(String emptyValueTransfer) {
            this.emptyValueTransfer = emptyValueTransfer;
            return this;
        }

        public Calculator.Builder withPatten(String patten) {
            this.patten = patten;
            return this;
        }

        public Calculator.Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public Calculator.Builder withStep(int step) {
            this.step = step;
            return this;
        }

        /**
         * build.
         */
        public Calculator build() {
            Calculator calculator = new Calculator();
            calculator.calculateType = this.calculateType;
            calculator.expression = this.expression;
            if (null != calculator.expression && !calculator.expression.isEmpty()) {
                calculator.code = MD5Utils.encrypt(calculator.expression);
            }
            calculator.validator = this.validator;
            calculator.min = this.min;
            calculator.max = this.max;
            calculator.condition = this.condition;
            calculator.emptyValueTransfer = this.emptyValueTransfer;
            calculator.patten = this.patten;
            calculator.model = this.model;
            calculator.step = this.step;
            calculator.level = this.level;

            return calculator;
        }
    }
}
