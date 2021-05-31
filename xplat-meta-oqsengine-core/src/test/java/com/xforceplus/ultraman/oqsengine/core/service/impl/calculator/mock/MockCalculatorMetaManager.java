package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class MockCalculatorMetaManager implements MetaManager {

    //-------------level 0--------------------
    public static final IEntityClass L0_ENTITY_CLASS = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("l0")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.LONG)
            .withName("longValue0")
            .withConfig(FieldConfig.build().searchable(true))
            .withCalculator(Calculator.Builder.anCalculator()
                .withCalculateType(Calculator.Type.NORMAL)
                .build())
            .build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1)
            .withFieldType(FieldType.LONG)
            .withName("longValue1")
            .withConfig(FieldConfig.build().searchable(true))
            .withCalculator(Calculator.Builder.anCalculator()
                .withCalculateType(Calculator.Type.FORMULA)
                                .withLevel(1)
                                .withExpression("${longValue0} * 3")
                                .build())
            .build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 2)
            .withFieldType(FieldType.LONG)
            .withName("longValue2")
            .withConfig(FieldConfig.build().searchable(true))
            .withCalculator(Calculator.Builder.anCalculator()
                .withCalculateType(Calculator.Type.FORMULA)
                .withLevel(2)
                .withExpression("${longValue1} / 2")
                .build())
            .build())
        .build();

    //-------------level 1--------------------
    public static IEntityClass L1_ENTITY_CLASS = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("l1")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 3)
            .withFieldType(FieldType.DATETIME)
            .withName("dateValue0")
            .withConfig(FieldConfig.build().searchable(true))
            .withCalculator(Calculator.Builder.anCalculator()
                .withCalculateType(Calculator.Type.FORMULA)
                .withLevel(1)
                .withExpression("now()")
                .build())
            .build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 4)
            .withFieldType(FieldType.STRING)
            .withName("stringAutoFill")
            .withConfig(FieldConfig.build().searchable(true))
            .withCalculator(Calculator.Builder.anCalculator()
                .withCalculateType(Calculator.Type.AUTO_FILL)
            .build())
            .build())
        .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 5)
                .withFieldType(FieldType.STRING)
                .withName("stringValueMix")
                .withConfig(FieldConfig.build().searchable(true))
                .withCalculator(Calculator.Builder.anCalculator()
                    .withCalculateType(Calculator.Type.FORMULA)
                    .withLevel(2)
                    .withExpression("string.join(seq.list(${longValue0}, ${stringAutoFill}), '-')")
                    .build())
            .build())
        .withFather(L0_ENTITY_CLASS)
        .build();

    private final Collection<IEntityClass> entities;

    public MockCalculatorMetaManager() {
        entities = Arrays.asList(
            L0_ENTITY_CLASS, L1_ENTITY_CLASS
        );
    }

    @Override
    public Optional<IEntityClass> load(long id) {
        return entities.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {
        return entities.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.empty();
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public void invalidateLocal() {

    }
}
