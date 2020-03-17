package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Arrays;
import java.util.Collection;

/**
 * DefaultSphinxQLConditionCompareStrategy Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/16/2020
 * @since <pre>Mar 16, 2020</pre>
 */
public class DefaultSphinxQLConditionCompareStrategyTest {

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
     * Method: build(String prefix, Condition condition, StorageStrategyFactory storageStrategyFactory)
     */
    @Test
    public void testBuild() throws Exception {
        DefaultSphinxQLConditionCompareStrategy strategy = new DefaultSphinxQLConditionCompareStrategy();
        buildCases().stream().forEach(c -> {
            String where = strategy.build(c.prefix, c.condition, storageStrategyFactory);
            Assert.assertEquals(c.expected, where);
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                "jsonfields",
                new Condition(
                    new Field(1, "c1", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(
                        new Field(1, "c1", FieldType.LONG),
                        100L
                    )
                ),
                "jsonfields.1L = 100"
            ),

            new Case(
                "jsonfields",
                new Condition(
                    new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.EQUALS,
                    new LongValue(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        100L
                    )
                ),
                "id = 100"
            ),

            new Case(
                "jsonfields",
                new Condition(
                    new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        1L
                    ),
                    new LongValue(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        2L
                    ),
                    new LongValue(
                        new Field(1, "c1", FieldType.LONG, FieldConfig.build().identifie(true)),
                        3L
                    )
                ),
                "id IN (1,2,3)"
            )
        );
    }

    private static class Case {
        private String prefix;
        private Condition condition;
        private String expected;

        public Case(String prefix, Condition condition, String expected) {
            this.prefix = prefix;
            this.condition = condition;
            this.expected = expected;
        }
    }

} 
