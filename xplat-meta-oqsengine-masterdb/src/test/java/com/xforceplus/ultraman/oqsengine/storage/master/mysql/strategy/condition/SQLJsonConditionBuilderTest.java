package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * SQLJsonConditionBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 11/05/2020
 * @since <pre>Nov 5, 2020</pre>
 */
public class SQLJsonConditionBuilderTest {

    private static IEntityField idField =
        new EntityField(Long.MAX_VALUE, "id", FieldType.LONG, FieldConfig.build().identifie(true));
    private static IEntityField longField = new EntityField(1, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(2, "string", FieldType.STRING);
    private static IEntityField wildCardStringField = EntityField.Builder.anEntityField()
        .withId(3)
        .withName("wildcard-string")
        .withFieldType(FieldType.STRING)
        .withConfig(FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.WILDCARD).build()).build();
    private static IEntityField segmentationStringField = EntityField.Builder.anEntityField()
        .withId(4)
        .withName("segmentation-string")
        .withFieldType(FieldType.STRING)
        .withConfig(FieldConfig.Builder.anFieldConfig().withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION).build())
        .build();

    @Test
    public void testBuildCondition() throws Exception {

        buildCases().stream().forEach(c -> {
            SQLJsonConditionBuilder builder =
                new SQLJsonConditionBuilder(c.condition.getField().type(), c.condition.getOperator());
            builder.setStorageStrategyFactory(StorageStrategyFactory.getDefaultFactory());

            try {
                builder.setTokenizerFacotry(new DefaultTokenizerFactory());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            Assertions.assertEquals(c.expectedSql, builder.build(c.condition), c.desc);
        });
    }

    static class Case {
        private String desc;
        private Condition condition;
        private String expectedSql;

        public Case(String desc, Condition condition, String expectedSql) {
            this.desc = desc;
            this.condition = condition;
            this.expectedSql = expectedSql;
        }
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                "id eq 100",
                new Condition(
                    idField,
                    ConditionOperator.EQUALS,
                    new LongValue(idField, 100L)
                ),
                "id = 100"
            ),
            new Case(
                "id in 100",
                new Condition(
                    idField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(idField, 100L)
                ),
                "id IN (100)"
            ),
            new Case(
                "in 100 200",
                new Condition(
                    idField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(idField, 100L),
                    new LongValue(idField, 200L)
                ),

                "id IN (100,200)"
            ),
            new Case(
                "long eq 200",
                new Condition(
                    longField,
                    ConditionOperator.EQUALS,
                    new LongValue(longField, 200L)
                ),
                "CAST(attribute->>'$.F1L' AS SIGNED) = 200"
            ),
            new Case(
                "long in 200 300",
                new Condition(
                    longField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(longField, 200L),
                    new LongValue(longField, 300L)
                ),
                "CAST(attribute->>'$.F1L' AS SIGNED) IN (200,300)"
            ),
            new Case(
                "string in 200 300",
                new Condition(
                    stringField,
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringValue(stringField, "200L"),
                    new StringValue(stringField, "300L")
                ),
                "attribute->>'$.F2S' IN (\"200L\",\"300L\")"
            ),
            new Case(
                "long <= 200",
                new Condition(
                    longField,
                    ConditionOperator.LESS_THAN_EQUALS,
                    new LongValue(longField, 200L)
                ),
                "CAST(attribute->>'$.F1L' AS SIGNED) <= 200"
            ),
            new Case(
                "long >= 200",
                new Condition(
                    longField,
                    ConditionOperator.GREATER_THAN_EQUALS,
                    new LongValue(longField, 200L)
                ),
                "CAST(attribute->>'$.F1L' AS SIGNED) >= 200"
            ),
            new Case(
                "string like 200",
                new Condition(
                    stringField,
                    ConditionOperator.LIKE,
                    new StringValue(stringField, "200L")
                ),
                "2 = 1"
            ),
            new Case(
                "wildcard string like 186213",
                new Condition(
                    wildCardStringField,
                    ConditionOperator.LIKE,
                    new StringValue(wildCardStringField, "186213")
                ),
                "attribute->>'$.F3S' LIKE \"%186213%\""
            ),
            new Case(
                "wildcard string like 18",
                new Condition(
                    wildCardStringField,
                    ConditionOperator.LIKE,
                    new StringValue(wildCardStringField, "18")
                ),
                "2 = 1"
            ),
            new Case(
                "segmentation string like -",
                new Condition(
                    segmentationStringField,
                    ConditionOperator.LIKE,
                    new StringValue(segmentationStringField, "-")
                ),
                "attribute->>'$.F4S' LIKE \"%-%\""
            ),
            new Case(
                "segmentation string like 这是一个测试",
                new Condition(
                    segmentationStringField,
                    ConditionOperator.LIKE,
                    new StringValue(segmentationStringField, "这是一个测试")
                ),
                "attribute->>'$.F4S' LIKE \"%这是%一个%测试%\""
            ),
            new Case(
                "segmentation string like -这是一个测试",
                new Condition(
                    segmentationStringField,
                    ConditionOperator.LIKE,
                    new StringValue(segmentationStringField, "-这是一个测试")
                ),
                "attribute->>'$.F4S' LIKE \"%这是%一个%测试%\""
            )
        );
    }
}
