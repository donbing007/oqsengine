package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class StaticCalculation extends AbstractCalculation {

    public static final int DEFAULT_LEVEL = 1;

    private StaticCalculation() {
        super(CalculationType.STATIC);
    }

    @Override
    public AbstractCalculation clone() {
        return new StaticCalculation();
    }

    /**
     * builder.
     */
    public static final class Builder {

        private Builder() {
        }

        public static StaticCalculation.Builder anStaticCalculation() {
            return new StaticCalculation.Builder();
        }

        /**
         * build.
         */
        public StaticCalculation build() {
            StaticCalculation staticCalculation = new StaticCalculation();
            staticCalculation.calculationType = CalculationType.STATIC;
            staticCalculation.level = DEFAULT_LEVEL;
            return staticCalculation;
        }
    }
}
