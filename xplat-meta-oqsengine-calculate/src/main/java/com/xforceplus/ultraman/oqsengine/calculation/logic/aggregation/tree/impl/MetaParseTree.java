package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 元数据解析树.
 *
 * @className: ParseTreeImpl
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:57
 */
public class MetaParseTree implements ParseTree {
    private Logger logger = LoggerFactory.getLogger(MetaParseTree.class);

    private String prefix;

    private PTNode root;

    public MetaParseTree() {

    }

    public MetaParseTree(PTNode root) {
        this.root = root;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public PTNode getRoot() {
        return root;
    }

    public void setRoot(PTNode root) {
        this.root = root;
    }


    @Override
    public PTNode root() {
        return root;
    }

    @Override
    public int treeLevel() {
        return 0;
    }


    @Override
    public List<PTNode> toList() {
        return getLevelList().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public void add(PTNode node) {
        if (node == null) {
            logger.warn("add a null node,refuse");
            return;
        }
        // 是否是根节点
        if (node.isRootFlag()) {
            this.root = node;
            return;
        } else if (root == null) {
            logger.error("please set root node first, then add again");
        } else {
            Queue<PTNode> queue = new LinkedList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                int size = queue.size();
                for (int i = 0; i < size; i++) {
                    PTNode ptNode = queue.poll();
                    if (ptNode != null) {
                        // 如果找到对应聚合当前entityClass和entityField节点，将该节点加入到找到节点的子节点集合中
                        if (ptNode.getEntityClass().equals(node.getAggEntityClass()) && ptNode.getEntityField().equals(node.getEntityField())) {
                            ptNode.getNextNodes().add(node);
                            break;
                        }
                    }
                    queue.addAll(ptNode.getNextNodes());
                }
            }
            logger.error(String.format("can not find relation node in the tree by entityClassId %d and entityField %d", node.getAggEntityClass().id(), node.getAggEntityField().id()));
        }
    }

    @Override
    public List<ParseTree> getSubTree(IEntityClass entityClass, IEntityField entityField) {
        // 返回树集合
        List<ParseTree> trees = new ArrayList<>();
        Queue<PTNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                PTNode ptNode = queue.poll();
                if (ptNode != null) {
                    // 如果找到对应聚合当前entityClass和entityField节点，将该节点子树加入到结果树集合中
                    if (ptNode.getAggEntityClass().equals(entityClass) && ptNode.getAggEntityField().equals(entityField)) {
                        trees.add(new MetaParseTree(ptNode));
                    }
                }
                queue.addAll(ptNode.getNextNodes());
            }
        }
        return trees;
    }

    @Override
    public ParseTree generateTree(List<PTNode> nodes) {
        List<PTNode> collect = nodes.stream().filter(PTNode::isRootFlag).collect(Collectors.toList());
        if (collect.size() != 1) {
            logger.error("nodesList's rootNode size not equals 1, must have only one rootNode");
            return null;
        }
        // 获取集合中的root node
        PTNode root = collect.get(0);
        nodes.remove(root);

        Queue<PTNode> queue = new LinkedList<>();
        queue.add(root);
        while (nodes.size() > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                PTNode ptNode = queue.poll();

                // 构建下一层node
                for (int j = 0; j < nodes.size(); j++) {
                    if (nodes.get(j).getAggEntityClass().equals(ptNode.getEntityClass()) && nodes.get(j).getAggEntityField().equals(ptNode.getEntityField())) {
                        ptNode.getNextNodes().add(nodes.get(j));
                        queue.add(nodes.get(j));
                    }
                }

                // 表示当前node有被聚合
                if (ptNode.getNextNodes().size() > 0) {
                    nodes.removeAll(ptNode.getNextNodes());
                }

            }
            // 表示遍历完最后一层树
            // 之后没有任何叶子节点可以加，但是集合中还有未添加node
            if (queue.size() <= 0 && nodes.size() > 0) {
                logger.error(String.format("this nodeLists has some node without relation."));
                return null;
            }
        }
        return new MetaParseTree(root);
    }

    // 获取每层Node集合
    @Override
    public List<List<PTNode>> getLevelList() {
        if (root == null) {
            return null;
        }
        List<List<PTNode>> nodes = new ArrayList<>();
        Queue<PTNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            List<PTNode> level = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                PTNode node = queue.poll();
                // 添加到下一层
                level.add(node);
                queue.addAll(node.getNextNodes());
            }
            // 将每层node列表加到集合
            nodes.add(level);
        }
        return nodes;
    }

}
