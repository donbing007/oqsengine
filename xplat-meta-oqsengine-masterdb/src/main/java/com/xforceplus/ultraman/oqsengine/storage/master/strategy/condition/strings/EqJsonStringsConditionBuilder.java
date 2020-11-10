package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.strings;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * 等于.
 *
 * @author dongbin
 * @version 0.1 2020/11/10 11:17
 * @since 1.8
 */
public class EqJsonStringsConditionBuilder extends AbstractJsonStringsConditionBuilder {

    public EqJsonStringsConditionBuilder(StorageStrategyFactory storageStrategyFactory) {
        super(ConditionOperator.EQUALS, storageStrategyFactory);
    }

    @Override
    public String actualOperator() {
        return ConditionOperator.LIKE.getSymbol();
    }
}
