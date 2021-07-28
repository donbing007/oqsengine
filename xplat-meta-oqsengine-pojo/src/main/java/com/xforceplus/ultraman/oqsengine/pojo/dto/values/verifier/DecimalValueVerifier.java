package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.math.BigDecimal;

/**
 * 浮点数校验.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 15:30
 * @since 1.8
 */
public class DecimalValueVerifier implements ValueVerifier {

    @Override
    public boolean isTooLong(IEntityField field, IValue value) {
        BigDecimal decimal = (BigDecimal) value.getValue();
        int len = NumberUtils.size(decimal.longValue());
        len += decimal.scale();
        // 额外的小数点.
        len++;

        return !(len > field.config().getLen());
    }


    @Override
    public boolean isHighPrecision(IEntityField field, IValue value) {
        BigDecimal decimal = (BigDecimal) value.getValue();

        int precision = field.config().getPrecision();
        /*
         * 最小精度为1,至少有一个.0
         */
        if (precision == 0) {
            precision = 1;
        }

        int scale = decimal.scale();
        return !(scale > precision);
    }
}
