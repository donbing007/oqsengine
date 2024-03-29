package com.xforceplus.ultraman.oqsengine.metadata.utils;

import static com.xforceplus.ultraman.oqsengine.metadata.utils.storage.EntityClassStorageBuilderUtils.toFieldTypeValue;

import com.google.protobuf.Any;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ProtoAnyHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ProtoAnyHelperTest {

    private final Long expectedLong = 1L;
    private final long expectedLongValue = 2L;

    private final Boolean expectedBoolean = false;
    private final boolean expectedBooleanValue = true;

    private final String expectedString = "test";
    private final String expectedStrings = "test1, test2, test3, test4";

    private final Long expectedTimestamp = System.currentTimeMillis();

    @Test
    public void packUnPack() throws Exception {
        compare(expectedLong, FieldType.LONG);
        compare(expectedLongValue, FieldType.LONG);

        compare(expectedBoolean, FieldType.BOOLEAN);
        compare(expectedBooleanValue, FieldType.BOOLEAN);

        compare(expectedString, FieldType.STRING);
        compare(expectedString, FieldType.ENUM);

        compare(expectedStrings, FieldType.STRINGS);

        compare(expectedTimestamp, FieldType.DATETIME);
    }

    private <T> void compare(T expected, FieldType fieldType) throws Exception {
        Optional<Any> t = ProtoAnyHelper.toAnyValue(expected);
        Assertions.assertTrue(t.isPresent());

        Optional<?> v = toFieldTypeValue(fieldType, t.get());
        Assertions.assertTrue(v.isPresent());
        switch (fieldType) {
            case DATETIME: {
                Assertions.assertEquals(expected, ((LocalDateTime) v.get()).toInstant(ZoneOffset.of("+8")).toEpochMilli());
                break;
            }
            case STRINGS: {
                Assertions.assertEquals(expected, String.join(",", (String[]) v.get()));
                break;
            }
            default: {
                Assertions.assertEquals(expected, v.get());
            }
        }

    }
}
