package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula.codeGenerate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.List;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class AutoFill extends AbstractCalculation {

    @JsonProperty(value = "patten")
    private String patten;

    @JsonProperty(value = "model")
    private String model;

    @JsonProperty(value = "step")
    private int step;

    @JsonProperty(value = "min")
    private long min;

    @JsonProperty(value = "max")
    private long max;

    /**
     * senior auto fill use params.
     */
    @JsonProperty(value = "code")
    protected String code;

    @JsonProperty(value = "expression")
    private String expression;

    @JsonProperty(value = "level")
    private int level;

    @JsonProperty(value = "args")
    private List<String> args;

    @JsonProperty(value = "domainNoSenior")
    private DomainNoType domainNoType;

    @JsonProperty(value = "resetType")
    private int resetType;

    /**
     * getter/setter
     */
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

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExpression() {
        return expression;
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

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public DomainNoType getDomainNoType() {
        return domainNoType;
    }

    public void setDomainNoType(
        DomainNoType domainNoType) {
        this.domainNoType = domainNoType;
    }

    public int getResetType() {
        return resetType;
    }

    public void setResetType(int resetType) {
        this.resetType = resetType;
    }

    private AutoFill() {
        super(CalculationType.AUTO_FILL);
    }

    @Override
    public AbstractCalculation clone() {
        AutoFill autoFill = new AutoFill();
        autoFill.step = this.step;
        autoFill.model = this.model;
        autoFill.patten = this.patten;
        autoFill.max = this.max;
        autoFill.min = this.min;
        autoFill.code = this.code;
        autoFill.expression = this.expression;
        autoFill.level = this.level;
        autoFill.args = this.args;
        autoFill.domainNoType = this.domainNoType;
        autoFill.resetType = this.resetType;

        return autoFill;
    }

    /**
     * builder.
     */
    public static final class Builder {
        private String patten;
        private String model;
        private int step;
        private long min;
        private long max;
        private String expression;
        private int level;
        private List<String> args;
        private DomainNoType domainNoType;
        private int resetType;

        private Builder() {
        }

        public static AutoFill.Builder anAutoFill() {
            return new AutoFill.Builder();
        }


        public AutoFill.Builder withPatten(String patten) {
            this.patten = patten;
            return this;
        }

        public AutoFill.Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public AutoFill.Builder withStep(int step) {
            this.step = step;
            return this;
        }

        public AutoFill.Builder withMin(long min) {
            this.min = min;
            return this;
        }

        public AutoFill.Builder withMax(long max) {
            this.max = max;
            return this;
        }

        public AutoFill.Builder withExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public AutoFill.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public AutoFill.Builder withArgs(List<String> args) {
            this.args = args;
            return this;
        }

        public AutoFill.Builder withDomainNoType(DomainNoType domainNoType) {
            this.domainNoType = domainNoType;
            return this;
        }

        public AutoFill.Builder withResetType(int resetType) {
            this.resetType = resetType;
            return this;
        }

        /**
         * build.
         */
        public AutoFill build() {
            AutoFill autoFill = new AutoFill();
            autoFill.calculationType = CalculationType.AUTO_FILL;
            autoFill.patten = patten;
            autoFill.model = model;
            autoFill.step = step;
            autoFill.min = min;
            autoFill.max = max;
            if (null != expression && !expression.isEmpty()) {
                autoFill.expression = this.expression;
                autoFill.code = codeGenerate(expression);
            }
            autoFill.level = this.level;
            autoFill.args = this.args;
            autoFill.domainNoType = this.domainNoType;
            autoFill.resetType = this.resetType;

            return autoFill;
        }
    }

    /**
     * domainNoType.
     */
    public enum DomainNoType {
        UNKNOWN(0),
        NORMAL(1),
        SENIOR(2);

        private int type;

        DomainNoType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        /**
         * instance.
         */
        public static AutoFill.DomainNoType instance(int type) {
            for (AutoFill.DomainNoType domainNoType : AutoFill.DomainNoType.values()) {
                if (domainNoType.getType() == type) {
                    return domainNoType;
                }
            }

            return UNKNOWN;
        }
    }
}
