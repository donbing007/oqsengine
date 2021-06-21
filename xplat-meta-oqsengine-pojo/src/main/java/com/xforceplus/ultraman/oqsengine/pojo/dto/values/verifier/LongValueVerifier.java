package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.common.number.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 数值型字段校验.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 14:54
 * @since 1.8
 */
public class LongValueVerifier implements ValueVerifier {

    @Override
    public boolean isTooLong(IEntityField field, IValue value) {

        return !(NumberUtils.size(value.valueToLong()) > field.config().getLen());
    }

}
