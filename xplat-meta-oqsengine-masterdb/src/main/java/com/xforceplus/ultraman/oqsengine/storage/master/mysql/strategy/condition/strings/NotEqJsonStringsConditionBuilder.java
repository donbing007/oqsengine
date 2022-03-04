package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.condition.strings;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 不等于.
 *
 * @author dongbin
 * @version 0.1 2020/11/10 11:24
 * @since 1.8
 */
public class NotEqJsonStringsConditionBuilder extends AbstractJsonStringsConditionBuilder {

    public NotEqJsonStringsConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.NOT_EQUALS, storageStrategyFactory);
    }


    @Override
    public String actualOperator() {
        return "NOT LIKE";
    }
}
