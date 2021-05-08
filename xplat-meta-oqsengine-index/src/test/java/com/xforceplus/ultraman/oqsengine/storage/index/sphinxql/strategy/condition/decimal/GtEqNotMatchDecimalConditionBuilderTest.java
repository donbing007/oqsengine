package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * GtEqNotMatchDecimalConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since
 * 
 *        <pre>
 * Mar 26, 2020
 *        </pre>
 */
public class GtEqNotMatchDecimalConditionBuilderTest {

	private StorageStrategyFactory storageStrategyFactory;

	@Before
	public void before() throws Exception {
		storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
		storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
	}

	@After
	public void after() throws Exception {
	}

	@Test
	public void testBuild() throws Exception {
		GtEqNotMatchDecimalConditionBuilder
            builder = new GtEqNotMatchDecimalConditionBuilder(storageStrategyFactory);
		IEntityField field = new EntityField(1, "test", FieldType.DECIMAL);
		String conditionSql = builder.build(new Condition(field, ConditionOperator.GREATER_THAN_EQUALS,
				new DecimalValue(field, new BigDecimal("123.456"))));
        Assert.assertEquals("((" + FieldDefine.ATTRIBUTE + ".1L0 > 123) OR (" + FieldDefine.ATTRIBUTE
            + ".1L0 = 123 AND " + FieldDefine.ATTRIBUTE + ".1L1 >= 456000000000000000))", conditionSql);

	}

}
