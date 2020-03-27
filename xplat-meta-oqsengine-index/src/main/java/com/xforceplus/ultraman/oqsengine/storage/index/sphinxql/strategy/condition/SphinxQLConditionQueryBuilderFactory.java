package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.GtEqNotMatchDecimalConditionQueryBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.GtNotMatchDecimalConditionQueryBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.LtEqNotMatchDecimalConditionQueryBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.LtNotMatchDecimalConditionQueryBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 10:22
 * @since 1.8
 */
public class SphinxQLConditionQueryBuilderFactory {

    private StorageStrategyFactory storageStrategyFactory;
    private ConcurrentMap<String, SphinxQLConditionQueryBuilder> builders;

    public SphinxQLConditionQueryBuilderFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
        init();
    }

    private static String buildKey(FieldType fieldType, ConditionOperator operator, boolean match, boolean id) {
        return fieldType.getType() + "-" + operator.getSymbol() + "-" + match + "-" + id;
    }

    /**
     * @see FieldType
     * @see ConditionOperator
     */
    private void init() {
        builders = new ConcurrentHashMap<>();


        // decimal
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN, false, false),
            new GtNotMatchDecimalConditionQueryBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN_EQUALS, false, false),
            new GtEqNotMatchDecimalConditionQueryBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN, false, false),
            new LtNotMatchDecimalConditionQueryBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN_EQUALS, false, false),
            new LtEqNotMatchDecimalConditionQueryBuilder(storageStrategyFactory)
        );

        // meq
        builders.put(
            buildKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionQueryBuilder(storageStrategyFactory, FieldType.LONG, false)
        );
        builders.put(
            buildKey(FieldType.STRING, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionQueryBuilder(storageStrategyFactory, FieldType.STRING, false)
        );
        builders.put(
            buildKey(FieldType.BOOLEAN, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionQueryBuilder(storageStrategyFactory, FieldType.BOOLEAN, false)
        );

        // long
        builders.put(
            buildKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, false, true),
            new MeqNotMatchConditionQueryBuilder(storageStrategyFactory, FieldType.LONG)
        );

        // enum
        builders.put(
            buildKey(FieldType.ENUM, ConditionOperator.EQUALS, true, false),
            new MatchConditionQueryBuilder(storageStrategyFactory, FieldType.ENUM, ConditionOperator.EQUALS, true)
        );
        builders.put(
            buildKey(FieldType.ENUM, ConditionOperator.NOT_EQUALS, true, false),
            new MatchConditionQueryBuilder(storageStrategyFactory, FieldType.ENUM, ConditionOperator.NOT_EQUALS, true)
        );
        builders.put(
            buildKey(FieldType.ENUM, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionQueryBuilder(storageStrategyFactory, FieldType.ENUM, true)
        );
    }

    public SphinxQLConditionQueryBuilder getQueryBuilder(Condition condition, boolean match) {

        String key = buildKey(
            condition.getField().type(), condition.getOperator(), match, condition.getField().config().isIdentifie());

        SphinxQLConditionQueryBuilder builder = builders.get(key);
        if (builder == null) {

            synchronized (key) {
                builder = builders.get(key);

                if (builder == null) {

                    if (match) {
                        builder = new MatchConditionQueryBuilder(
                            storageStrategyFactory, condition.getField().type(), condition.getOperator(), false);
                    } else {

                        builder = new NotMatchConditionQueryBuilder(
                            storageStrategyFactory, condition.getField().type(), condition.getOperator());
                    }

                    builders.put(key, builder);
                }
            }

        }

        return builder;
    }
}
