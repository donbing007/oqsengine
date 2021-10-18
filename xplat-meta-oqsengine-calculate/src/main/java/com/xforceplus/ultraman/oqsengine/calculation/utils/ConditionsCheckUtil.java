package com.xforceplus.ultraman.oqsengine.calculation.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * 数据筛选工具
 *
 * @className: ConditionsCheckUtil.
 * @package: com.xforceplus.ultraman.oqsengine.calculation.utils.
 * @author: wangzheng.
 * @date: 2021/10/18 15:26.
 */
public class ConditionsCheckUtil {
    public boolean check(IEntity entity, IEntityClass entityClass, Conditions conditions) {
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        conditions.addAnd(new Condition(entityClass.field("id").get(),
                ConditionOperator.EQUALS, entity.entityValue().getValue(entity.id()).get()));
        
        return false;
    }
}
