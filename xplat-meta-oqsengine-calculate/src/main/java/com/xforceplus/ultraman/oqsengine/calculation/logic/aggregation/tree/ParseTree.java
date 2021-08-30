package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTCondition;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTRoot;

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

    PTRoot root();

    PTCondition showNodeCondition();

    int treeLevel();
}
