package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * MatchConditionQueryBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MatchConditionBuilderTest {

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
     * Method: build(Condition condition)
     */
    @Test
    public void testBuild() throws Exception {

        buildCases().stream().forEach(c -> {
            MatchConditionBuilder builder = new MatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator(), c.useGroupName);


            String result = builder.build(c.condition);
            c.check.accept(result);
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                r -> {
                    Assert.assertEquals("1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 200L)
                ),
                r -> {
                    Assert.assertEquals("1y2p0ij20032e8e7L", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new EntityField(9223372036854775807L, "test", FieldType.DECIMAL), new BigDecimal("123.246"))
                ),
                r -> {
                    Assert.assertEquals("(1y2p0ij12332e8e7L0 1y2p0ij24600000000000000032e8e7L1)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                r -> {
                    Assert.assertEquals("-1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.LIKE,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                r -> {
                    Assert.assertEquals("(1y2p0ij << *test* << 32e8e7S)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "test")
                ),
                r -> {
                    Assert.assertEquals("1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.NOT_EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "test")
                ),
                r -> {
                    Assert.assertEquals("-1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                    ConditionOperator.NOT_EQUALS,
                    new DecimalValue(new EntityField(9223372036854775807L, "test", FieldType.DECIMAL), BigDecimal.ZERO)
                ),
                r -> {
                    Assert.assertEquals("-(1y2p0ij032e8e7L0 1y2p0ij032e8e7L1)", r);
                }
            ),
            // 多值只处理第一个值.
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRINGS),
                    ConditionOperator.EQUALS,
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "v1"),
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "v2")
                ),
                true,
                r -> {
                    Assert.assertEquals("1y2p0ijv132e8e7S*", r);
                }
            )
        );
    }

    private static class Case {
        private Condition condition;
        private boolean useGroupName;
        private Consumer<? super String> check;

        public Case(Condition condition, Consumer<? super String> check) {
            this(condition, false, check);
        }

        public Case(Condition condition, boolean useGroupName, Consumer<? super String> check) {
            this.condition = condition;
            this.useGroupName = useGroupName;
            this.check = check;
        }
    }


} 
