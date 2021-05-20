package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.HaveOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

/**
 * 有OR和范围查询的条件生成测试.
 *
 * @author dongbin
 * @version 0.1 2021/04/09 11:45
 * @since 1.8
 */
public class HaveOrHaveRanageConditionsBuilderTest {

    private static IEntityClass entityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withCode("test")
        .withField(EntityField.CREATE_TIME_FILED)
        .withField(EntityField.UPDATE_TIME_FILED)
        .build();

    @Test
    public void testBuild() throws Exception {
        HaveOrHaveRanageConditionsBuilder builder = new HaveOrHaveRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);
        builder.init();


        buildCase().forEach(c -> {
            String where = builder.build(c.conditions, entityClass).toString();
            Assert.assertEquals(c.expected, where);
        });
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            // c1 or c2
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 100L)
                        )
                    )
                    .addOr(
                        new Condition(
                            EntityField.UPDATE_TIME_FILED,
                            ConditionOperator.NOT_EQUALS,
                            new LongValue(EntityField.UPDATE_TIME_FILED, 200L)
                        )
                    ),
                String.format("(createtime = 100 OR updatetime != 200)",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE)
            ),
            // c1 or c2 or (c3 and c4)
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 100L)
                        )
                    )
                    .addOr(
                        new Condition(
                            EntityField.UPDATE_TIME_FILED,
                            ConditionOperator.NOT_EQUALS,
                            new LongValue(EntityField.UPDATE_TIME_FILED, 200L)
                        )
                    ).addOr(
                    Conditions.buildEmtpyConditions()
                        .addAnd(
                            new Condition(
                                EntityField.CREATE_TIME_FILED,
                                ConditionOperator.EQUALS,
                                new LongValue(EntityField.UPDATE_TIME_FILED, 300L)
                            )
                        )
                        .addAnd(
                            new Condition(
                                EntityField.UPDATE_TIME_FILED,
                                ConditionOperator.EQUALS,
                                new LongValue(EntityField.UPDATE_TIME_FILED, 400L)
                            )
                        ),
                    true
                ),
                String.format(
                    "(createtime = 100 OR updatetime != 200 OR (updatetime = 300 AND updatetime = 400))",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE
                )
            ),
            // (c1 and c2) or c3
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.UPDATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.UPDATE_TIME_FILED, 100L)
                        )
                    )
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 300L)
                        )
                    ).close()
                    .addOr(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 500L)
                        )
                    ),
                String.format("((updatetime = 100 AND createtime = 300) OR createtime = 500)",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE
                )
            ),
            // (c1 and c2) or (c3 and c4)
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.UPDATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.UPDATE_TIME_FILED, 100L)
                        )
                    )
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 300L)
                        )
                    ).close().addOr(
                    Conditions.buildEmtpyConditions()
                        .addAnd(
                            new Condition(
                                EntityField.CREATE_TIME_FILED,
                                ConditionOperator.EQUALS,
                                new LongValue(EntityField.CREATE_TIME_FILED, 500L)
                            )
                        )
                        .addAnd(
                            new Condition(
                                EntityField.CREATE_TIME_FILED,
                                ConditionOperator.EQUALS,
                                new LongValue(EntityField.CREATE_TIME_FILED, 600L)
                            )
                        ),
                    true
                ),
                String.format(
                    "((updatetime = 100 AND createtime = 300) OR (createtime = 500 AND createtime = 600))",
                    FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE, FieldDefine.ATTRIBUTE
                )
            )
        );
    }

    static class Case {
        private Conditions conditions;
        private String expected;

        public Case(Conditions conditions, String expected) {
            this.conditions = conditions;
            this.expected = expected;
        }
    }

}