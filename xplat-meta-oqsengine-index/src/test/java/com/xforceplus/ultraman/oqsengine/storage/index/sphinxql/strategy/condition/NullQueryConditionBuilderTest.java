package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 属性是否存在的查询条件构造器测试.
 */
public class NullQueryConditionBuilderTest {

    private StorageStrategyFactory storageStrategyFactory;

    @BeforeEach
    public void before() throws Exception {
        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    @Test
    public void testIsNullBuilder() throws Exception {
        NullQueryConditionBuilder builder = new NullQueryConditionBuilder(FieldType.STRING, ConditionOperator.IS_NULL);
        builder.setStorageStrategyFactory(storageStrategyFactory);

        IEntityField field = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.STRING)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withIdentifie(false).build()
            )
            .build();

        Condition condition = new Condition(field, ConditionOperator.IS_NULL, new EmptyTypedValue(field));
        Assertions.assertEquals(
            String.format("%s.%s is null",
                FieldDefine.ATTRIBUTE,
                storageStrategyFactory.getStrategy(FieldType.STRING)
                    .toStorageNames(field, true).stream().findFirst().get()),
            builder.build(condition));
    }

    @Test
    public void testIsNotNullBuilder() throws Exception {
        NullQueryConditionBuilder builder =
            new NullQueryConditionBuilder(FieldType.STRING, ConditionOperator.IS_NOT_NULL);
        builder.setStorageStrategyFactory(storageStrategyFactory);

        IEntityField field = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.STRING)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withIdentifie(false).build()
            )
            .build();

        Condition condition = new Condition(field, ConditionOperator.IS_NOT_NULL, new EmptyTypedValue(field));
        Assertions.assertEquals(
            String.format("%s.%s is not null",
                FieldDefine.ATTRIBUTE,
                storageStrategyFactory.getStrategy(FieldType.STRING)
                    .toStorageNames(field, true).stream().findFirst().get()),
            builder.build(condition));
    }

}