package com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/11/5 17:20
 * @since 1.8
 */
public class SQLJsonConditionsBuilderFactory implements StorageStrategyFactoryAble {


    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;
    private ConditionsBuilder<String> conditionsBuilder;

    @PostConstruct
    public void init() {
        SQLJsonConditionsBuilder cb = new SQLJsonConditionsBuilder();
        cb.setStorageStrategy(storageStrategyFactory);

        if (Lifecycle.class.isInstance(cb)) {
            try {
                ((Lifecycle) cb).init();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }

        conditionsBuilder = cb;
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public ConditionsBuilder<String> getBuilder() {
        return conditionsBuilder;
    }
}
