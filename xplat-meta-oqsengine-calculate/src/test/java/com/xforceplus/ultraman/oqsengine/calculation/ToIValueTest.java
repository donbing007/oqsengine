package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class ToIValueTest {
    public static final IEntityField ROUND_DOWN =
        EntityField.Builder.anEntityField()
            .withId(5)
            .withName("decimal")
            .withFieldType(FieldType.DECIMAL)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                        .withPrecision(3)
                        .withScale(IValueUtils.Scale.ROUND_DOWN.getScale())
                        .build()
            ).build();

    public static final IEntityField ROUND_HALF_UP =
        EntityField.Builder.anEntityField()
            .withId(5)
            .withName("decimal")
            .withFieldType(FieldType.DECIMAL)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withPrecision(3)
                    .withScale(IValueUtils.Scale.ROUND_HALF_UP.getScale())
                    .build()
            ).build();

    @Test
    public void test() {
        IValue<?> value = IValueUtils.toIValue(ROUND_DOWN, new BigDecimal("123.454963474"));
        Assertions.assertEquals(new BigDecimal("123.454"), value.getValue());

        value = IValueUtils.toIValue(ROUND_HALF_UP, new BigDecimal("123.454963474"));
        Assertions.assertEquals(new BigDecimal("123.455"), value.getValue());

        value = IValueUtils.toIValue(ROUND_HALF_UP, new BigDecimal("123.4544"));
        Assertions.assertEquals(new BigDecimal("123.454"), value.getValue());
    }
}
