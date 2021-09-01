package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;

import java.util.List;

/**
 * 解析树节点.
 *
 * @className: PTNode
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:49
 */
public class PTNode {

    /**
     * 根节点标识.
     */
    private boolean rootFlag;

    /**
     * 节点层次.
     */
    private int level;

    /**
     * 节点对象信息.
     */
    private IEntityClass entityClass;

    /**
     * 节点字段信息.
     */
    private IEntityField entityField;

    /**
     * 节点条件信息.
     */
    private Condition condition;

    /**
     * 关系信息
     */
    private Relationship relationship;

    /**
     * 子节点.
     */
    private List<PTNode> nextNodes;

    /**
     * 父节点.
     */
    private PTNode preNode;
}
