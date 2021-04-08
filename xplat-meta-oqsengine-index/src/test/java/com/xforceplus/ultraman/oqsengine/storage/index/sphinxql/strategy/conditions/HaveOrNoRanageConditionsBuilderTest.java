package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author dongbin
 * @version 0.1 2021/04/07 16:54
 * @since 1.8
 */
public class HaveOrNoRanageConditionsBuilderTest {

    private static IEntityClass entityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withCode("test")
        .withField(EntityField.CREATE_TIME_FILED)
        .withField(EntityField.UPDATE_TIME_FILED)
        .build();

    @Test
    public void testBuild() throws Exception {
        HaveOrNoRanageConditionsBuilder builder = new HaveOrNoRanageConditionsBuilder();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        builder.setStorageStrategy(storageStrategyFactory);
        builder.init();


        buildCase().stream().forEach(c -> {
            String where = builder.build(entityClass, c.conditions);
            Assert.assertEquals(c.expected, where);
        });
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 100L)
                        )
                    ).addOr(
                    new Condition(
                        EntityField.UPDATE_TIME_FILED,
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(EntityField.UPDATE_TIME_FILED, 200L)
                    )
                ),
                String.format("MATCH('((@%s 1y2p0ij10032e8e6L) | (@%s =%d @%s -1y2p0ij20032e8e5L)) (@%s =%d)')",
                    FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id(),
                    FieldDefine.ATTRIBUTEF, FieldDefine.ENTITYCLASSF, entityClass.id())
            )
            ,
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.CREATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.CREATE_TIME_FILED, 100L)
                        )
                    ).addOr(
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
                        ).addAnd(
                        new Condition(
                            EntityField.UPDATE_TIME_FILED,
                            ConditionOperator.EQUALS,
                            new LongValue(EntityField.UPDATE_TIME_FILED, 400L)
                        )
                    ),
                    true
                ),
                ""
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