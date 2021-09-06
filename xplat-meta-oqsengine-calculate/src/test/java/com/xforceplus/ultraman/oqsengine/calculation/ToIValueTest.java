package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    public void testFlatMap() {
        List<Integer> list1 = new ArrayList();
        List<Integer> list2 = new ArrayList();
        List<Integer> list3 = new ArrayList();
        List<Integer> list4 = new ArrayList();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        list2.add(4);
        list2.add(5);
        list2.add(6);
        list3.add(7);
        list3.add(8);
        list3.add(9);
        list4.add(10);
        list4.add(11);
        list4.add(12);
        List<List<Integer>> list = new ArrayList<>();
        list.add(list1);
        list.add(list2);
        list.add(list3);
        list.add(list4);
        List<Integer> collect = list.stream().flatMap(List::stream).collect(Collectors.toList());
        System.out.println(collect.toArray().toString());
        for (int i = 0; i < collect.size(); i++) {
            Assertions.assertTrue(collect.get(i).intValue() == (i+1));
        }
    }

}
