package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 17:28
 * @since 1.8
 */
public class HaveOrNoRanageConditionsBuilder implements ConditionsBuilder<String> {
    @Override
    public void init() {

    }

    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {
        // 还未实现.
        throw new UnsupportedOperationException();
    }
}
