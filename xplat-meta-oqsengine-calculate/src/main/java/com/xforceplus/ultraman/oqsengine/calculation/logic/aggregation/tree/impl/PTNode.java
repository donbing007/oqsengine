package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 解析树节点.
 *
 * @className: PTNode
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:49
 */
public class PTNode {
    private boolean rootFlag;
    private int level;
    private IEntityClass entityClass;
    private IEntityField entityField;
}
