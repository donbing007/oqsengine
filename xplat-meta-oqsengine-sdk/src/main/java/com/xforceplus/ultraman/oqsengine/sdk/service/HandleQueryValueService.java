package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;

/**
 * 条件查询.
 */
public interface HandleQueryValueService {

    ConditionsUp handleQueryValue(IEntityClass entityClass, Conditions request, OperationType phase);
}
