package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * JSON的条件查询构造器工厂.
 *
 * @author dongbin
 * @version 0.1 2020/11/5 17:20
 * @since 1.8
 */
public class SQLJsonConditionsBuilderFactory implements StorageStrategyFactoryAble, TokenizerFactoryAble, Lifecycle {

    private StorageStrategyFactory storageStrategyFactory;
    private TokenizerFactory tokenizerFactory;

    private ConditionsBuilder<Conditions, String> conditionsBuilder;

    @PostConstruct
    public void init() throws Exception {
        SQLJsonConditionsBuilder cb = new SQLJsonConditionsBuilder();
        cb.setStorageStrategyFactory(storageStrategyFactory);
        cb.setTokenizerFacotry(tokenizerFactory);
        cb.init();

        conditionsBuilder = cb;
    }

    @Resource(name = "masterStorageStrategy")
    @Override
    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Resource(name = "tokenizerFactory")
    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }

    public ConditionsBuilder<Conditions, String> getBuilder() {
        return conditionsBuilder;
    }
}
