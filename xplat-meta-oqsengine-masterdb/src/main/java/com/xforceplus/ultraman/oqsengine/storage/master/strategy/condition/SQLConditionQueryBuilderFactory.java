package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal.GtDecimalJsonConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal.GtEqDecimalJsonConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal.LtDecimalJsonConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.decimal.LtEqDecimalJsonConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.strings.EqJsonStringsConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.strings.MeqJsonStringsConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.strings.NotEqJsonStringsConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

/**
 * 单条件构造器工厂.
 *
 * @author dongbin
 * @version 0.1 2020/11/5 10:45
 * @since 1.8
 */
public class SQLConditionQueryBuilderFactory implements TokenizerFactoryAble, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private TokenizerFactory tokenizerFactory;
    private Map<String, ConditionBuilder> builders;


    @Override
    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }

    @PostConstruct
    public void init() throws Exception {
        builders = new ConcurrentHashMap();

        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN),
            new GtDecimalJsonConditionBuilder(storageStrategyFactory)
        );

        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN_EQUALS),
            new GtEqDecimalJsonConditionBuilder(storageStrategyFactory)
        );

        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN),
            new LtDecimalJsonConditionBuilder(storageStrategyFactory)
        );

        builders.put(
            buildKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN_EQUALS),
            new LtEqDecimalJsonConditionBuilder(storageStrategyFactory)
        );

        //  strings
        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.EQUALS),
            new EqJsonStringsConditionBuilder(storageStrategyFactory)
        );

        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.NOT_EQUALS),
            new NotEqJsonStringsConditionBuilder(storageStrategyFactory)
        );

        builders.put(
            buildKey(FieldType.STRINGS, ConditionOperator.MULTIPLE_EQUALS),
            new MeqJsonStringsConditionBuilder(storageStrategyFactory)
        );
    }

    /**
     * 根据条件定义得到条件的查询构造器.
     *
     * @param condition 查询条件.
     * @return 条件查询构造器.
     */
    public ConditionBuilder getQueryBuilder(Condition condition) {
        final String key = buildKey(condition.getField().type(), condition.getOperator());

        ConditionBuilder builder = builders.get(key);
        if (builder == null) {
            synchronized (key) {
                builder =
                    new SQLJsonConditionBuilder(
                        condition.getField().type(), condition.getOperator());

                ((SQLJsonConditionBuilder) builder).setStorageStrategyFactory(storageStrategyFactory);
                ((SQLJsonConditionBuilder) builder).setTokenizerFacotry(tokenizerFactory);

                builders.put(key, builder);
            }
        }

        return builder;
    }

    private String buildKey(FieldType fieldType, ConditionOperator operator) {
        StringBuilder key = new StringBuilder();
        return key.append(fieldType.getType()).append(".").append(operator.getSymbol()).toString();
    }
}
