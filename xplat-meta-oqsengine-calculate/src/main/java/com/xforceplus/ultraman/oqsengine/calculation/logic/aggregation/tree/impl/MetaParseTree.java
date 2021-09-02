package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.List;
import java.util.Map;

/**
 * 元数据解析树.
 *
 * @className: ParseTreeImpl
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:57
 */
public class MetaParseTree implements ParseTree {
    private PTNode node;

    public MetaParseTree (PTNode ptNode) {
        this.node = ptNode;
    }

    @Override
    public PTNode next() {
        return null;
    }

    @Override
    public ParseTree getTree() {
        return this;
    }

    @Override
    public PTNode getNode() {
        return null;
    }

    @Override
    public PTNode root() {
        return null;
    }

    @Override
    public Condition showNodeCondition() {

        return null;
    }

    @Override
    public int treeLevel() {

        return 0;
    }

    @Override
    public List<ParseTree> getTrees(String appId) {
        return null;
    }

    @Override
    public boolean saveTrees(List<ParseTree> trees) {
        return false;
    }

    @Override
    public List<PTNode> toList() {
        return null;
    }

    @Override
    public void add(PTNode node) {

    }

    @Override
    public ParseTree getSubTree(IEntityClass entityClass, IEntityField entityField) {
        return null;
    }

    @Override
    public ParseTree generateTree(Map map) {
        return null;
    }
}
