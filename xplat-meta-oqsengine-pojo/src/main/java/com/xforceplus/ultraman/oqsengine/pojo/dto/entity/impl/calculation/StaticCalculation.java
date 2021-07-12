package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class StaticCalculation extends AbstractCalculation {
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
            return staticCalculation;
        }
    }
}
