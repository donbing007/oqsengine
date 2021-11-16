package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * MatchConditionQueryBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 03/26/2020
 * @since <pre>Mar 26, 2020</pre>
 */
public class MatchConditionBuilderTest {

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
            MatchConditionBuilder builder = new MatchConditionBuilder(
                storageStrategyFactory, c.condition.getField().type(), c.condition.getOperator(), c.useGroupName);
            try {
                builder.setTokenizerFacotry(new DefaultTokenizerFactory());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }


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
                    Assertions.assertEquals("1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(9223372036854775807L, "test", FieldType.LONG), 200L)
                ),
                r -> {
                    Assertions.assertEquals("1y2p0ij20032e8e7L", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                    ConditionOperator.EQUALS,
                    new DecimalValue(new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                        new BigDecimal("123.246"))
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0ij12332e8e7L0 1y2p0ij24600000000000000032e8e7L1)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.STRING),
                    ConditionOperator.NOT_EQUALS,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                r -> {
                    Assertions.assertEquals("-1y2p0ijtest32e8e7S", r);
                }
            ),
            // 没有打开排序,所以退化成精确匹配.
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test",
                        FieldType.STRING, FieldConfig.build().fuzzyType(FieldConfig.FuzzyType.WILDCARD)),
                    ConditionOperator.LIKE,
                    new StringValue(new EntityField(9223372036854775807L, "test", FieldType.STRING), "test")
                ),
                r -> {
                    Assertions.assertEquals("1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    EntityField.Builder.anEntityField()
                        .withFieldType(FieldType.STRING)
                        .withId(9223372036854775807L)
                        .withConfig(FieldConfig.build().fuzzyType(FieldConfig.FuzzyType.SEGMENTATION))
                        .build(),
                    ConditionOperator.LIKE,
                    new StringValue(EntityField.Builder.anEntityField()
                        .withFieldType(FieldType.STRING)
                        .withId(9223372036854775807L)
                        .withConfig(FieldConfig.build().fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build(),
                        "工作状态有限公司")
                ),
                r -> {
                    Assertions.assertEquals("(1y2p0ij工作w32e8e7S << 1y2p0ij状态w32e8e7S << 1y2p0ij有限公司w32e8e7S)", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "test")
                ),
                r -> {
                    Assertions.assertEquals("1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.ENUM),
                    ConditionOperator.NOT_EQUALS,
                    new EnumValue(new EntityField(9223372036854775807L, "test", FieldType.ENUM), "test")
                ),
                r -> {
                    Assertions.assertEquals("-1y2p0ijtest32e8e7S", r);
                }
            ),
            new Case(
                new Condition(
                    new EntityField(9223372036854775807L, "test", FieldType.DECIMAL),
                    ConditionOperator.NOT_EQUALS,
                    new DecimalValue(new EntityField(9223372036854775807L, "test", FieldType.DECIMAL), BigDecimal.ZERO)
                ),
                r -> {
                    Assertions.assertEquals("-(1y2p0ij032e8e7L0 1y2p0ij032e8e7L1)", r);
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
                    Assertions.assertEquals("1y2p0ijv132e8e7S", r);
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
