package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

/**
 * 条件数量小于1的处理.
 *
 * @author dongbin
 * @version 0.1 2020/2/28 09:11
 * @since 1.8
 */
public class EmptyConditionsBuilder implements ConditionsBuilder<String> {

    @Override
    public String build(Conditions conditions) {
        return "MATCH('@fullfields F*')";
    }
}
