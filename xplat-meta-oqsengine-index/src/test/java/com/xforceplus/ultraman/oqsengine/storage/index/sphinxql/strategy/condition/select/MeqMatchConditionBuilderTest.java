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
            builder.setStorageStrategy(storageStrategyFactory);

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
                    Assertions.assertEquals("(1y2p0ijtest032e8e7S | 1y2p0ijtest132e8e7S | 1y2p0ijtest232e8e7S)", r);
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
                    Assertions.assertEquals("(1y2p0ij132e8e7L | 1y2p0ij232e8e7L | 1y2p0ij332e8e7L)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "one")
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0ijone32e8e7S)", r);
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
                    Assertions.assertEquals("(1y2p0ijone32e8e7S | 1y2p0ijtwo32e8e7S)", r);
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
