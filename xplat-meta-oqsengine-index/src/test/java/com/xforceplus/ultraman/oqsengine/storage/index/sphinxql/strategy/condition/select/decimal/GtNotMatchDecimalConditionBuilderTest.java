package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * GtNotMatchDecimalConditionQueryBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class GtNotMatchDecimalConditionBuilderTest {

    private StorageStrategyFactory storageStrategyFactory;

    @BeforeEach
    public void before() throws Exception {
        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    @AfterEach
    public void after() throws Exception {
    }


    @Test
    public void testBuild() throws Exception {
        GtNotMatchDecimalConditionBuilder
            builder = new GtNotMatchDecimalConditionBuilder(storageStrategyFactory);
        IEntityField field = new EntityField(1, "test", FieldType.DECIMAL);
        String conditionSql = builder.build(
            new Condition(
                field,
                ConditionOperator.GREATER_THAN,
                new DecimalValue(field, new BigDecimal("123.456")
                )
            )
        );

        Assertions.assertEquals("((" + FieldDefine.ATTRIBUTE + ".1L0 > 123) OR (" + FieldDefine.ATTRIBUTE
            + ".1L0 = 123 AND " + FieldDefine.ATTRIBUTE + ".1L1 > 456000000000000000))", conditionSql);

    }


} 
