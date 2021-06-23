package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 校验定义.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 14:13
 * @since 1.8
 */
public interface ValueVerifier {

    /**
     * 进行校验.
     *
     * @param value 需要校验的目标值.
     * @return 校验结果.
     */
    public default VerifierResult verify(IEntityField field, IValue value) {
        if (!isRequired(field, value)) {
            return VerifierResult.REQUIRED;
        }

        if (value != null) {

            if (!isTooLong(field, value)) {
                return VerifierResult.TOO_LONG;
            }

            if (!isHighPrecision(field, value)) {
                return VerifierResult.HIGH_PRECISION;
            }
        }

        return VerifierResult.OK;
    }

    /**
     * 校验是否必须但是却没有设置合适的值.
     *
     * @param field 目标字段.
     * @param value 目标字段值.
     * @return true 合式,false不合式.
     */
    public default boolean isRequired(IEntityField field, IValue value) {
        FieldConfig config = field.config();
        if (config.isRequired()) {
            if (value == null || EmptyTypedValue.class.isInstance(value)) {
                return false;
            }
        }

        return true;
    }

    public default boolean isTooLong(IEntityField field, IValue value) {
        return true;
    }

    public default boolean isHighPrecision(IEntityField field, IValue value) {
        return true;
    }
}
