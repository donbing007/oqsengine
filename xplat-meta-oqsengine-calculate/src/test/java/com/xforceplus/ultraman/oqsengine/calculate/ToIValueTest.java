package com.xforceplus.ultraman.oqsengine.calculate;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class ToIValueTest {
    public static final IEntityField DECIMAL_FIELD =
        new EntityField(5, "decimal", FieldType.DECIMAL,
            FieldConfig.Builder.anFieldConfig().withPrecision(3).withSearchable(true).build(), null, null);

    @Test
    public void testRoundDown() {
        IValue<?> value = IValueUtils.toIValue(DECIMAL_FIELD, new BigDecimal("123.454963474"));
        Assert.assertEquals(new BigDecimal("123.454"), value.getValue());
    }
}
