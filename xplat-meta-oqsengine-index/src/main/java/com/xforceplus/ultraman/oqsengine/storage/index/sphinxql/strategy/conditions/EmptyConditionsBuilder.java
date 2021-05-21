package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

/**
 * 空条件的处理.
 *
 * @author dongbin
 * @version 0.1 2020/2/28 09:11
 * @since 1.8
 */
public class EmptyConditionsBuilder implements ConditionsBuilder<SphinxQLWhere> {

    @Override
    public SphinxQLWhere build(IEntityClass entityClass, Conditions conditions) {
        return new SphinxQLWhere();
    }
}
