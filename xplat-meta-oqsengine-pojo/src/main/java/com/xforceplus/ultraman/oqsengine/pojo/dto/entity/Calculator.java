package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.utils.MD5Utils;
import java.util.List;

/**
 * Created by justin.xu on 05/2021
 */
public class Calculator {

    public static final int DEFAULT_FORMULA_LEVEL = 1;

    @JsonProperty(value = "type")
    private Type type;

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

    @JsonProperty(value = "args")
    private List<String> args;

    @JsonProperty(value = "failedPolicy")
    private FailedPolicy failedPolicy;

    @JsonProperty(value = "failedDefaultValue")
    private Object failedDefaultValue;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getExpression() {
        return expression;
    }

    public Calculator.Type getType() {
        return type;
    }

    public void setType(Calculator.Type type) {
        this.type = type;
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

    public FailedPolicy getFailedPolicy() {
        return failedPolicy;
    }

    public void setFailedPolicy(FailedPolicy failedPolicy) {
        this.failedPolicy = failedPolicy;
    }

    public Object getFailedDefaultValue() {
        return failedDefaultValue;
    }

    public void setFailedDefaultValue(Object failedDefaultValue) {
        this.failedDefaultValue = failedDefaultValue;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private Calculator.Type type;
        private String expression;
        private Integer level = DEFAULT_FORMULA_LEVEL;
        private String validator;
        private String min;
        private String max;
        private String condition;
        private String emptyValueTransfer;
        private String patten;
        private String model;
        private int step;
        private List<String> args;
        private Calculator.FailedPolicy failedPolicy;
        private Object failedDefaultValue;

        private Builder() {
        }

        public static Calculator.Builder anCalculator() {
            return new Calculator.Builder();
        }

        public Calculator.Builder withCalculateType(Calculator.Type calculateType) {
            this.type = calculateType;
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

        public Calculator.Builder withArgs(List<String> args) {
            this.args = args;
            return this;
        }

        public Calculator.Builder withFailedPolicy(Calculator.FailedPolicy failedPolicy) {
            this.failedPolicy = failedPolicy;
            return this;
        }

        public Calculator.Builder withFailedDefaultValue(Object failedDefaultValue) {
            this.failedDefaultValue = failedDefaultValue;
            return this;
        }

        /**
         * build.
         */
        public Calculator build() {
            Calculator calculator = new Calculator();
            calculator.type = this.type;
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
            calculator.args = this.args;
            calculator.failedPolicy = this.failedPolicy;
            calculator.failedDefaultValue = this.failedDefaultValue;

            return calculator;
        }
    }

    /**
     * 失败处理策略.
     */
    public enum FailedPolicy {
        UNKNOWN(0),
        THROW_EXCEPTION(1),
        USE_FAILED_DEFAULT_VALUE(2);

        private final int policy;

        FailedPolicy(int policy) {
            this.policy = policy;
        }

        public int getPolicy() {
            return policy;
        }

        /**
         * instance.
         */
        public static FailedPolicy instance(int policy) {
            for (FailedPolicy failedPolicy : FailedPolicy.values()) {
                if (failedPolicy.policy == policy) {
                    return failedPolicy;
                }
            }

            return UNKNOWN;
        }
    }


    /**
     * 计算类型.
     */
    public enum Type {
        UNKNOWN(0),
        NORMAL(1),
        FORMULA(2),
        AUTO_FILL(3);

        private final int type;

        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        /**
         * instance.
         */
        public static Type instance(int type) {
            for (Type calculateType : Type.values()) {
                if (calculateType.type == type) {
                    return calculateType;
                }
            }

            return UNKNOWN;
        }
    }
}
