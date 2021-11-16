package com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.SQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import java.sql.SQLException;

/**
 * 基于json的搜索构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:49
 * @since 1.8
 */
public class SQLJsonConditionsBuilder
    implements ConditionsBuilder<Conditions, String>, StorageStrategyFactoryAble, TokenizerFactoryAble, Lifecycle {

    private StorageStrategyFactory storageStrategyFactory;
    private TokenizerFactory tokenizerFactory;
    private SQLConditionQueryBuilderFactory sqlConditionQueryBuilderFactory;

    @Override
    public void init() throws Exception {
        this.sqlConditionQueryBuilderFactory = new SQLConditionQueryBuilderFactory();
        this.sqlConditionQueryBuilderFactory.setStorageStrategy(storageStrategyFactory);
        this.sqlConditionQueryBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        this.sqlConditionQueryBuilderFactory.init();
    }

    @Override
    public String build(Conditions conditions, IEntityClass ...entityClasses) {
        if (conditions.isEmtpy()) {
            return "";
        }

        StringBuilder sql = new StringBuilder();
        conditions.scan(
            link -> sql.append(" ").append(link.getLink().name()).append(" "),
            value -> {
                Condition condition = value.getCondition();
                ConditionBuilder<Condition, String> cb = sqlConditionQueryBuilderFactory.getQueryBuilder(condition);
                sql.append(cb.build(condition));
            },
            parenthese -> sql.append(parenthese.toString())
        );

        return sql.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }
}
