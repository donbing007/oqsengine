package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 17:29
 * @since 1.8
 */
public class HaveOrHaveRanageConditionsBuilder implements ConditionsBuilder<String> {

    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {
        //还未实现 by dongbin.
        throw new UnsupportedOperationException();
    }
}
