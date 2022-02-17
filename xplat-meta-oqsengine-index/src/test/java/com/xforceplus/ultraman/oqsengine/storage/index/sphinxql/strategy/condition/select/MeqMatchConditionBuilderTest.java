package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * MeqMatchConditionQueryBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MeqMatchConditionBuilderTest {

    private StorageStrategyFactory storageStrategyFactory;

    @BeforeEach
    public void before() throws Exception {
        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
    }

    @AfterEach
    public void after() throws Exception {
    }

    /**
     * Method: build(Condition condition).
     */
    @Test
    public void testBuild() throws Exception {

        buildCases().stream().forEach(c -> {
            MeqMatchConditionBuilder builder =
                new MeqMatchConditionBuilder(c.condition.getField().type(), c.useGroupName);
            builder.setStorageStrategyFactory(storageStrategyFactory);

            String result = builder.build(c.condition);
            c.check.accept(result);
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test0"),
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test1"),
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test2")
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0itest0j32e8e7S | 1y2p0itest1j32e8e7S | 1y2p0itest2j32e8e7S)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 1L),
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 2L),
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 3L)
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0i1j32e8e7L | 1y2p0i2j32e8e7L | 1y2p0i3j32e8e7L)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "one")
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0ionej32e8e7S)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRINGS),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "one"),
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "two")
                ),
                true,
                r -> {
                    Assertions.assertEquals("(1y2p0ionej32e8e7S | 1y2p0itwoj32e8e7S)", r);
                }
            ),

            /**
             * 这个测试为了测试超长的strings分割后的结果是否正确.
             */
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRINGS),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "AAAAAAAAAAAAAAAAAAAAAAAAAAAAABBB"),
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBCCC"),
                    new StringsValue(new EntityField(9223372036854775807L, "test", FieldType.STRINGS), "DDD")
                ),
                true,
                r -> {
                    Assertions.assertEquals("((P01y2p0iAAAAAAAAAAAAAAAAAAAAAAAAAj32e8e7S P11y2p0iAAAABBBj32e8e7S) | (P01y2p0iBBBBBBBBBBBBBBBBBBBBBBBBBj32e8e7S P11y2p0iBBBBCCCj32e8e7S) | 1y2p0iDDDj32e8e7S)", r);
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
