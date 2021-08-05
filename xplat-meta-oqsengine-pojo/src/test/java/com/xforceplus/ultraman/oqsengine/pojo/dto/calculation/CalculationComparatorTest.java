package com.xforceplus.ultraman.oqsengine.pojo.dto.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class CalculationComparatorTest {

    private static Long FORMULA_START_ID = 401L;
    private static Long AUTOFILL_START_ID = 301L;
    private static Long LOOKUP_START_ID = 201L;
    private static Long NO_START_ID = 101L;
    private static List<Long> EXPECTED = Arrays.asList(
        NO_START_ID, NO_START_ID + 1, LOOKUP_START_ID + 1, LOOKUP_START_ID,
        AUTOFILL_START_ID, FORMULA_START_ID, FORMULA_START_ID + 2, FORMULA_START_ID + 1
    );

    /**
     * 构造需要的数据.
     */
    public static class NeedSortEntityField {
        public static final IEntityClass NEED_SORT_CLASS =
            OqsEntityClass.Builder.anEntityClass()
                .withId(Long.MAX_VALUE / 2)
                .withLevel(0)
                .withCode("needSortTest")
                .withField(EntityField.Builder.anEntityField()
                    .withId(FORMULA_START_ID + 1)
                    .withFieldType(FieldType.LONG)
                    .withName("formula-L2")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(Formula.Builder.anFormula().withLevel(2).build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(LOOKUP_START_ID + 1)
                    .withFieldType(FieldType.LONG)
                    .withName("lookup1")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(Lookup.Builder.anLookup().build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(NO_START_ID)
                    .withFieldType(FieldType.LONG)
                    .withName("static1")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(AUTOFILL_START_ID)
                    .withFieldType(FieldType.LONG)
                    .withName("autoFill1")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(AutoFill.Builder.anAutoFill().build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(NO_START_ID + 1)
                    .withFieldType(FieldType.LONG)
                    .withName("static2")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(FORMULA_START_ID)
                    .withFieldType(FieldType.LONG)
                    .withName("formula-L1")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(Formula.Builder.anFormula().withLevel(1).build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(FORMULA_START_ID + 2)
                    .withFieldType(FieldType.LONG)
                    .withName("formula-L2")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(Formula.Builder.anFormula().withLevel(1).build())
                        .build()
                    )
                    .build()
                )
                .withField(EntityField.Builder.anEntityField()
                    .withId(LOOKUP_START_ID)
                    .withFieldType(FieldType.LONG)
                    .withName("lookup2")
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withLen(19)
                        .withSearchable(true)
                        .withRequired(true)
                        .withCalculation(Lookup.Builder.anLookup().build())
                        .build()
                    )
                    .build()
                )
                .build();
    }


    @Test
    public void testSort() {
        List<IEntityField> entityFields =
            NeedSortEntityField.NEED_SORT_CLASS.fields().stream()
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        for (int i = 0; i < entityFields.size(); i++) {
            Assertions.assertEquals(EXPECTED.get(i), entityFields.get(i).id());
        }
    }

}
