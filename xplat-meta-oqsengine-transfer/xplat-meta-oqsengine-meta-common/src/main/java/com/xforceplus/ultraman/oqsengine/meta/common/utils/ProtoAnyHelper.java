package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ProtoAnyHelper {

    /**
     * 转换Any类型.
     */
    public static <T> Optional<Any> toAnyValue(T t) {
        if (null == t) {
            return Optional.empty();
        }

        if (t instanceof Long) {
            return Optional.of(Any.pack(Int64Value.of((Long) t)));
        } else if (t instanceof Boolean) {
            return Optional.of(Any.pack(BoolValue.of((Boolean) t)));
        } else if (t instanceof String) {
            return Optional.of(Any.pack(StringValue.of((String) t)));
        } else if (t instanceof BigDecimal) {
            return Optional.of(Any.pack(DoubleValue.of(((BigDecimal) t).doubleValue())));
        }

        throw new IllegalArgumentException(String.format("un-support type %s-%s", t, t.getClass().getCanonicalName()));
    }

    /**
     * 按照FieldType转换成实际的Value值.
     */
    public static Optional<?> toFieldTypeValue(FieldType fieldType, Any any) throws Exception {
        Object value = null;
        if (any.isInitialized()) {
            switch (fieldType) {
                case DATETIME: {
                    value = DateTimeValue.toLocalDateTime(any.unpack(Int64Value.class).getValue());
                    break;
                }
                case LONG: {
                    value = any.unpack(Int64Value.class).getValue();
                    break;
                }
                case DECIMAL: {
                    value = BigDecimal.valueOf(any.unpack(DoubleValue.class).getValue());
                    break;
                }
                case STRING:
                case ENUM: {
                    value = any.unpack(StringValue.class).getValue();
                    break;
                }
                case BOOLEAN: {
                    value = any.unpack(BoolValue.class).getValue();
                    break;
                }
                case STRINGS: {
                    value = StringsValue.toStrings(any.unpack(StringValue.class).getValue());
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                        String.format("un-support type, fieldType : %s, protoTypeUrl : %s", fieldType.getType(),
                            any.getTypeUrl())
                    );
                }
            }
        }

        return Optional.ofNullable(value);
    }
}
