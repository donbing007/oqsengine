package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 多值字符串校验器.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:12
 * @since 1.8
 */
public class StringsValueVerifier implements ValueVerifier {

    @Override
    public boolean isTooLong(IEntityField field, IValue value) {
        String[] strs = (String[]) value.getValue();

        int len = 0;
        for (String s : strs) {
            len += s.length();
        }

        return !(len > field.config().getLen());
    }
}
