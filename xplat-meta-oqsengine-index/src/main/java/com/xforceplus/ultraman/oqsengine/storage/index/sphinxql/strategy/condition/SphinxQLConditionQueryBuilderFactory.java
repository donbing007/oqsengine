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
 * 查询条件构造器工厂.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 10:22
 * @since 1.8
 */
public class SphinxQLConditionQueryBuilderFactory implements TokenizerFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private ConcurrentMap<String, AbstractSphinxQLConditionBuilder> builders;
    private TokenizerFactory tokenizerFactory;

    public SphinxQLConditionQueryBuilderFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private static String buildKey(FieldType fieldType, ConditionOperator operator, boolean match, boolean id) {
        return fieldType.getType() + "-" + operator.getSymbol() + "-" + match + "-" + id;
    }

    /**
     * 初始化.
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
            new MatchConditionBuilder(storageStrategyFactory, FieldType.STRINGS, ConditionOperator.NOT_EQUALS,
                true)
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

    /**
     * 获取条件查询条件构造器实例.
     *
     * @param condition 条件.
     * @param match true 全文,false非全文.
     * @return 实例.
     */
    public AbstractSphinxQLConditionBuilder getQueryBuilder(Condition condition, boolean match) {

        String key = buildKey(
            condition.getField().type(), condition.getOperator(), match, condition.getField().config().isIdentifie());

        AbstractSphinxQLConditionBuilder builder = builders.computeIfAbsent(key, k -> {
            AbstractSphinxQLConditionBuilder b;
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
