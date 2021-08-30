package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;

/**
 * 元数据解析树.
 *
 * @className: ParseTreeImpl
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:57
 */
public class MetaParseTree implements ParseTree {

    @Override
    public PTNode next() {
        return null;
    }

    @Override
    public ParseTree getTree() {
        return null;
    }

    @Override
    public PTNode getNode() {
        return null;
    }

    @Override
    public PTRoot root() {
        return null;
    }

    @Override
    public PTCondition showNodeCondition() {

        return null;
    }

    @Override
    public int treeLevel() {

        return 0;
    }
}
