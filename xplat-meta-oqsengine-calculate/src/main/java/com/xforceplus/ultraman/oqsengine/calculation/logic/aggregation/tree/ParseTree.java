package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.List;
import java.util.Map;

/**
 * 聚合解析树
 *
 * @className: AggTree
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree
 * @author: wangzheng
 * @date: 2021/8/30 10:34
 */
public interface ParseTree {

    PTNode next();

    ParseTree getTree();

    PTNode getNode();

    PTNode root();

    Condition showNodeCondition();

    int treeLevel();

    List<ParseTree> getTrees(String appId);

    boolean saveTrees(List<ParseTree> trees);

    List<PTNode> toList();

    void add(PTNode node);

    ParseTree getSubTree(IEntityClass entityClass, IEntityField entityField);

    ParseTree generateTree(Map map);

}
