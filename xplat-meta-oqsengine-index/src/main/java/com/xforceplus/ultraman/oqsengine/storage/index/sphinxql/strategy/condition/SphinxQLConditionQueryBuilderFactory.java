package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.GtEqNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.GtNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.LtEqNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.decimal.LtNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author dongbin
 * @version 0.1 2020/3/26 10:22
 * @since 1.8
 */
public class SphinxQLConditionQueryBuilderFactory implements TokenizerFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private ConcurrentMap<String, SphinxQLConditionBuilder> builders;
    private TokenizerFactory tokenizerFactory;

    public SphinxQLConditionQueryBuilderFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private static String buildKey(FieldType fieldType, ConditionOperator operator, boolean match, boolean id) {
        return fieldType.getType() + "-" + operator.getSymbol() + "-" + match + "-" + id;
    }

    /**
     * @see FieldType
     * @see ConditionOperator
     */
    public void init() {
        builders = new ConcurrentHashMap<>();


        // decimal
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN, false, false),
            new GtNotMatchDecimalConditionBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN_EQUALS, false, false),
            new GtEqNotMatchDecimalConditionBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN, false, false),
            new LtNotMatchDecimalConditionBuilder(storageStrategyFactory)
        );
        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN_EQUALS, false, false),
            new LtEqNotMatchDecimalConditionBuilder(storageStrategyFactory)
        );

        // meq
        builders.put(
            buildKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(storageStrategyFactory, FieldType.LONG, false)
        );
        builders.put(
            buildKey(FieldType.STRING, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(storageStrategyFactory, FieldType.STRING, false)
        );
        builders.put(
            buildKey(FieldType.BOOLEAN, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(storageStrategyFactory, FieldType.BOOLEAN, false)
        );
        builders.put(
            buildKey(FieldType.ENUM, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(storageStrategyFactory, FieldType.ENUM, false)
        );

        // long
        builders.put(
            buildKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, false, true),
            new MeqNotMatchConditionBuilder(storageStrategyFactory, FieldType.LONG)
        );

        // strings
        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.EQUALS, true, false),
            new MatchConditionBuilder(storageStrategyFactory, FieldType.STRINGS, ConditionOperator.EQUALS, true)
        );
        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.NOT_EQUALS, true, false),
            new MatchConditionBuilder(storageStrategyFactory, FieldType.STRINGS, ConditionOperator.NOT_EQUALS, true)
        );
        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(storageStrategyFactory, FieldType.STRINGS, true)
        );

        builders.values().stream().forEach(b -> {
            if (TokenizerFactoryAble.class.isInstance(b)) {
                ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
            }
        });
    }

    public SphinxQLConditionBuilder getQueryBuilder(Condition condition, boolean match) {

        String key = buildKey(
            condition.getField().type(), condition.getOperator(), match, condition.getField().config().isIdentifie());

        SphinxQLConditionBuilder builder = builders.computeIfAbsent(key, k -> {
            SphinxQLConditionBuilder b;
            if (match) {
                b = new MatchConditionBuilder(
                    storageStrategyFactory, condition.getField().type(), condition.getOperator(), false);
            } else {

                b = new NotMatchConditionBuilder(
                    storageStrategyFactory, condition.getField().type(), condition.getOperator());
            }

            if (TokenizerFactoryAble.class.isInstance(b)) {
                ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
            }
            return b;
        });

        return builder;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }
}
