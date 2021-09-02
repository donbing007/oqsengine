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

    private String prefix;

    private PTNode node;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public PTNode getNode() {
        return node;
    }

    public void setNode(PTNode node) {
        this.node = node;
    }

    @Override
    public List<PTNode> next() {
        return null;
    }

    @Override
    public PTNode root() {
        return null;
    }

    @Override
    public int treeLevel() {
        return 0;
    }

    @Override
    public List<PTNode> toList() {
        return null;
    }

    @Override
    public void add(PTNode node) {

    }

    @Override
    public List<ParseTree> getSubTree(IEntityClass entityClass, IEntityField entityField) {
        return null;
    }

    @Override
    public ParseTree generateTree(List<PTNode> nodes) {
        return null;
    }
}
