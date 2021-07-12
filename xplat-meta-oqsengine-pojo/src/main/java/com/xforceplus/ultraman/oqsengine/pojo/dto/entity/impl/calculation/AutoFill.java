package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

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

    /**
     * builder.
     */
    public static final class Builder {
        private String patten;
        private String model;
        private int step;
        private long min;
        private long max;

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

            return autoFill;
        }
    }
}
