package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.utils.MD5Utils;
import java.util.List;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class Formula extends AbstractCalculation  {

    /**
     * 公式的唯一标示.
     */
    @JsonProperty(value = "code")
    protected String code;

    @JsonProperty(value = "expression")
    private String expression;

    @JsonProperty(value = "failedPolicy")
    private FailedPolicy failedPolicy = FailedPolicy.UNKNOWN;

    @JsonProperty(value = "failedDefaultValue")
    private Object failedDefaultValue;

    @JsonProperty(value = "level")
    private int level;

    @JsonProperty(value = "args")
    private List<String> args;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private Formula() {
        super(CalculationType.FORMULA);
    }

    @Override
    public AbstractCalculation clone() {
        Formula formula = new Formula();
        formula.args = this.args;
        formula.failedPolicy = this.failedPolicy;
        formula.failedDefaultValue = this.failedDefaultValue;
        formula.level = this.level;
        formula.expression = this.expression;
        formula.code = this.code;
        return formula;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private String expression;
        private FailedPolicy failedPolicy = FailedPolicy.UNKNOWN;
        private Object failedDefaultValue;
        private int level;
        private List<String> args;

        private Builder() {
        }

        public static Formula.Builder anFormula() {
            return new Formula.Builder();
        }

        public Formula.Builder withExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public Formula.Builder withFailedPolicy(FailedPolicy failedPolicy) {
            this.failedPolicy = failedPolicy;
            return this;
        }

        public Formula.Builder withFailedDefaultValue(Object failedDefaultValue) {
            this.failedDefaultValue = failedDefaultValue;
            return this;
        }

        public Formula.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public Formula.Builder withArgs(List<String> args) {
            this.args = args;
            return this;
        }

        /**
         * build.
         */
        public Formula build() {
            Formula formula = new Formula();
            formula.calculationType = CalculationType.FORMULA;
            formula.expression = expression;
            formula.code = codeGenerate(expression);
            formula.level = level;
            formula.args = args;
            formula.failedPolicy = failedPolicy;
            formula.failedDefaultValue = failedDefaultValue;

            return formula;
        }


    }

    public static String codeGenerate(String expression) {
        if (null != expression && !expression.isEmpty()) {
            return MD5Utils.encrypt(expression);
        }
        return "";
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
}
