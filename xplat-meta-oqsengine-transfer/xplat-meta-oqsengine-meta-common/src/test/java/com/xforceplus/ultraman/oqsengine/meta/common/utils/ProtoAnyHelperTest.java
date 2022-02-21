package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ProtoAnyHelperTest {

    @Test
    public void toAnyValueTest() throws InvalidProtocolBufferException {
        //  check long
        Long expectedLong = 1L;

        Optional<Any> anyOptional =
            ProtoAnyHelper.toAnyValue(expectedLong);

        Assertions.assertEquals(expectedLong,
            anyOptional.get().unpack(Int64Value.class).getValue());

        //  check boolean
        Boolean expectedBoolean = true;
        anyOptional =
            ProtoAnyHelper.toAnyValue(expectedBoolean);

        Assertions.assertEquals(expectedBoolean,
            anyOptional.get().unpack(BoolValue.class).getValue());

        //  check string
        String expectedString = "testString";
        anyOptional =
            ProtoAnyHelper.toAnyValue(expectedString);

        Assertions.assertEquals(expectedString,
            anyOptional.get().unpack(StringValue.class).getValue());

        //  check decimal
        BigDecimal expectedDecimal = new BigDecimal("100.00");
        anyOptional =
            ProtoAnyHelper.toAnyValue(expectedDecimal);

        Assertions.assertEquals(expectedDecimal.doubleValue(),
            anyOptional.get().unpack(DoubleValue.class).getValue());

        //  check exception
        Integer integer = 65;
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> ProtoAnyHelper.toAnyValue(integer));
    }
}
