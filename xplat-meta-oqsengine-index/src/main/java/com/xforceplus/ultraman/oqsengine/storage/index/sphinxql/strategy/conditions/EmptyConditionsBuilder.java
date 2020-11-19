package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

/**
 * 空条件的处理.
 *
 * @author dongbin
 * @version 0.1 2020/2/28 09:11
 * @since 1.8
 */
public class EmptyConditionsBuilder implements ConditionsBuilder<String> {

    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {
        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('@")
            .append(FieldDefine.ENTITY_F)
            .append(" =\"")
            .append(entityClass.id())
            .append("\"')");
        return buff.toString();
    }
}
