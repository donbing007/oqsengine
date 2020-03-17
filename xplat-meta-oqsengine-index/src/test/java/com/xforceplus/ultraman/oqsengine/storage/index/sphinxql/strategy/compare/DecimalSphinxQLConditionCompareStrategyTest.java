package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.JointMask;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * DecimalConditionCompareStrategy Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/06/2020
 * @since <pre>Mar 6, 2020</pre>
 */
public class DecimalSphinxQLConditionCompareStrategyTest {

    private StorageStrategyFactory storageStrategyFactory;

    @Before
    public void before() throws Exception {

        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(String fieldPrefix, Condition condition, StorageStrategy storageStrategy)
     */
    @Test
    public void testBuild() throws Exception {

        DecimalSphinxQLConditionCompareStrategy strategy = new DecimalSphinxQLConditionCompareStrategy();
        buildCase().stream().forEach(c -> {
            String where = strategy.build(c.prefix, c.condition, storageStrategyFactory);
            Assert.assertEquals(c.expected, where);
        });

    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new Field(1L, "c1", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new Field(1L, "c1", FieldType.DECIMAL), new BigDecimal("123.456"))
                ),
                "jsonfields",
                "jsonfields.1L0 = 123 " + JointMask.AND + " jsonfields.1L1 = 456"
            )
            ,
            new Case(
                new Condition(
                    new Field(1L, "c1", FieldType.DECIMAL),
                    ConditionOperator.GREATER_THAN,
                    new DecimalValue(new Field(1L, "c1", FieldType.DECIMAL), new BigDecimal("123.456"))
                ),
                "jsonfields",
                "jsonfields.1L0 >= 123 " + JointMask.AND + " jsonfields.1L1 > 456"
            )
            ,
            new Case(
                new Condition(
                    new Field(1L, "c1", FieldType.DECIMAL),
                    ConditionOperator.GREATER_THAN_EQUALS,
                    new DecimalValue(new Field(1L, "c1", FieldType.DECIMAL), new BigDecimal("123.456"))
                ),
                "jsonfields",
                "jsonfields.1L0 >= 123 " + JointMask.AND + " jsonfields.1L1 >= 456"
            )
            ,
            new Case(
                new Condition(
                    new Field(1L, "c1", FieldType.DECIMAL),
                    ConditionOperator.MINOR_THAN,
                    new DecimalValue(new Field(1L, "c1", FieldType.DECIMAL), new BigDecimal("123.456"))
                ),
                "jsonfields",
                "jsonfields.1L0 <= 123 " + JointMask.AND + " jsonfields.1L1 < 456"
            )
            ,
            new Case(
                new Condition(
                    new Field(1L, "c1", FieldType.DECIMAL),
                    ConditionOperator.MINOR_THAN_EQUALS,
                    new DecimalValue(new Field(1L, "c1", FieldType.DECIMAL), new BigDecimal("123.456"))
                ),
                "jsonfields",
                "jsonfields.1L0 <= 123 " + JointMask.AND + " jsonfields.1L1 <= 456"
            )
        );
    }

    static class Case {
        private Condition condition;
        private String prefix;
        private String expected;

        public Case(Condition condition, String prefix, String expected) {
            this.condition = condition;
            this.prefix = prefix;
            this.expected = expected;
        }
    }

} 
