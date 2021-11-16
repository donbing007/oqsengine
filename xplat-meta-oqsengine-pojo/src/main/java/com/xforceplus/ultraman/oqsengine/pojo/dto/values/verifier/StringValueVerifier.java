package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 字符串类型的校验.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 15:09
 * @since 1.8
 */
public class StringValueVerifier implements ValueVerifier {

    @Override
    public boolean isTooLong(IEntityField field, IValue value) {
        return !(value.valueToString().length() > field.config().getLen());
    }

}
