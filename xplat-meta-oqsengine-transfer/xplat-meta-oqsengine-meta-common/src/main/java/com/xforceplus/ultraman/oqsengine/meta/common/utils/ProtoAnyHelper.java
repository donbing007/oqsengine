package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
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
}
