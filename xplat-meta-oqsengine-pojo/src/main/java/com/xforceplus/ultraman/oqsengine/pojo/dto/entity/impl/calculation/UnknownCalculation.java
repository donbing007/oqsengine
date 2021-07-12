package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class UnknownCalculation extends AbstractCalculation {
    /**
     * builder.
     */
    public static final class Builder {

        private Builder() {
        }

        public static UnknownCalculation.Builder anUnknownCalculation() {
            return new UnknownCalculation.Builder();
        }

        /**
         * build.
         */
        public UnknownCalculation build() {
            UnknownCalculation unknownCalculation = new UnknownCalculation();
            unknownCalculation.calculationType = CalculationType.UNKNOWN;
            return unknownCalculation;
        }
    }
}
