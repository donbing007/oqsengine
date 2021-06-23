package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验器工厂.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:07
 * @since 1.8
 */
public class VerifierFactory {

    private static final ValueVerifier DEFAULT_VALUE_VERIFIER = new NoVerifier();
    private static Map<FieldType, ValueVerifier> VERIFIER_MAP;

    static {
        VERIFIER_MAP = new HashMap<>();
        VERIFIER_MAP.put(FieldType.STRING, new StringValueVerifier());
        VERIFIER_MAP.put(FieldType.LONG, new LongValueVerifier());
        VERIFIER_MAP.put(FieldType.DECIMAL, new DecimalValueVerifier());
        VERIFIER_MAP.put(FieldType.STRINGS, new StringsValueVerifier());
    }

    /**
     * 获取指定字段类型的校验器.
     *
     * @param type 逻辑字段类型.
     * @return 校验器.
     */
    public static ValueVerifier getVerifier(FieldType type) {
        ValueVerifier valueVerifier = VERIFIER_MAP.get(type);
        if (valueVerifier == null) {
            return DEFAULT_VALUE_VERIFIER;
        } else {
            return valueVerifier;
        }
    }

    /**
     * 默认实现.
     */
    static class NoVerifier implements ValueVerifier {

    }
}
