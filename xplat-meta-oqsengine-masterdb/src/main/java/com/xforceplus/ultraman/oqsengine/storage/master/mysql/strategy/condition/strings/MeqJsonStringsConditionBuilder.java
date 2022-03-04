package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.strings;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * in 查询的构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/10 11:30
 * @since 1.8
 */
public class MeqJsonStringsConditionBuilder extends AbstractJsonStringsConditionBuilder {


    public MeqJsonStringsConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.MULTIPLE_EQUALS, storageStrategyFactory);
    }

    @Override
    public String actualOperator() {
        return ConditionOperator.LIKE.getSymbol();
    }
}
