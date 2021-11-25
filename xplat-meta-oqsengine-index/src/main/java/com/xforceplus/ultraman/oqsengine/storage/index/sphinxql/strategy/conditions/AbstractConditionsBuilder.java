package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import java.sql.SQLException;

/**
 * 条件查询构造器抽像.
 *
 * @author dongbin
 * @version 0.1 2021/04/08 16:32
 * @since 1.8
 */
public abstract class AbstractConditionsBuilder
    implements ConditionsBuilder<Conditions, SphinxQLWhere>, StorageStrategyFactoryAble, TokenizerFactoryAble,
    Lifecycle {

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionQueryBuilderFactory conditionQueryBuilderFactory;

    private TokenizerFactory tokenizerFactory;

    @Override
    public void init() throws SQLException {
        this.conditionQueryBuilderFactory = new SphinxQLConditionQueryBuilderFactory();
        this.conditionQueryBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        this.conditionQueryBuilderFactory.setStorageStrategyFactory(this.storageStrategyFactory);
        this.conditionQueryBuilderFactory.init();
    }

    @Override
    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

    public SphinxQLConditionQueryBuilderFactory getConditionQueryBuilderFactory() {
        return conditionQueryBuilderFactory;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }

    public TokenizerFactory getTokenizerFactory() {
        return tokenizerFactory;
    }
}
