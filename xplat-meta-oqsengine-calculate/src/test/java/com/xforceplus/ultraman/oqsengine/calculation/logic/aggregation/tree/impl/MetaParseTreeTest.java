package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



/**
 * 聚合树测试.
 *
 * @author weikai
 * @version 1.0 2021/9/8 14:31
 * @since 1.8
 */
class MetaParseTreeTest {
    private PTNode root = new PTNode();
    private PTNode level1a = new PTNode();
    private PTNode level1b = new PTNode();
    private PTNode level1c = new PTNode();
    private PTNode level2a1 = new PTNode();
    private PTNode level2a2 = new PTNode();
    private PTNode level2b = new PTNode();
    private PTNode level2c1 = new PTNode();
    private PTNode level2c2 = new PTNode();
    private PTNode level2c3 = new PTNode();
    private List<PTNode> treeNodes;


    @BeforeEach
    public void before() {
        treeNodes = new ArrayList<>();

        root.setRootFlag(true);
        List<IEntityField> fields = new ArrayList<>();
        List<IEntityClass> entityClasses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            fields.add(EntityField.Builder.anEntityField().withId(i).build());
            entityClasses.add(EntityClass.Builder.anEntityClass().withId(i).withField(fields.get(i)).build());
        }
        root.setEntityClass(entityClasses.get(0));
        root.setEntityField(fields.get(0));
        EntityField build = EntityField.Builder.anEntityField().withId(100).build();
        root.setAggEntityClass(EntityClass.Builder.anEntityClass().withId(100).withField(build).build());
        root.setAggEntityField(build);
        treeNodes.add(root);

        level1a.setEntityClass(entityClasses.get(1));
        level1a.setEntityField(fields.get(1));
        level1a.setAggEntityField(root.getEntityField());
        level1a.setAggEntityClass(root.getEntityClass());
        treeNodes.add(level1a);


        level1b.setEntityClass(entityClasses.get(2));
        level1b.setEntityField(fields.get(2));
        level1b.setAggEntityField(root.getEntityField());
        level1b.setAggEntityClass(root.getEntityClass());
        treeNodes.add(level1b);

        level1c.setEntityClass(entityClasses.get(3));
        level1c.setEntityField(fields.get(3));
        level1c.setAggEntityField(root.getEntityField());
        level1c.setAggEntityClass(root.getEntityClass());
        treeNodes.add(level1c);

        level2a1.setEntityClass(entityClasses.get(4));
        level2a1.setEntityField(fields.get(4));
        level2a1.setAggEntityField(level1a.getEntityField());
        level2a1.setAggEntityClass(level1a.getEntityClass());
        treeNodes.add(level2a1);

        level2a2.setEntityClass(entityClasses.get(5));
        level2a2.setEntityField(fields.get(5));
        level2a2.setAggEntityField(level1a.getEntityField());
        level2a2.setAggEntityClass(level1a.getEntityClass());
        treeNodes.add(level2a2);

        level2b.setEntityClass(entityClasses.get(6));
        level2b.setEntityField(fields.get(6));
        level2b.setAggEntityField(level1b.getEntityField());
        level2b.setAggEntityClass(level1b.getEntityClass());
        treeNodes.add(level2b);

        level2c1.setEntityClass(entityClasses.get(7));
        level2c1.setEntityField(fields.get(7));
        level2c1.setAggEntityField(level1c.getEntityField());
        level2c1.setAggEntityClass(level1c.getEntityClass());
        treeNodes.add(level2c1);

        level2c2.setEntityClass(entityClasses.get(8));
        level2c2.setEntityField(fields.get(8));
        level2c2.setAggEntityField(level1c.getEntityField());
        level2c2.setAggEntityClass(level1c.getEntityClass());
        treeNodes.add(level2c2);

        level2c3.setEntityClass(entityClasses.get(9));
        level2c3.setEntityField(fields.get(9));
        level2c3.setAggEntityField(level1c.getEntityField());
        level2c3.setAggEntityClass(level1c.getEntityClass());
        treeNodes.add(level2c3);

    }


    @AfterEach
    public void destroy() {
        treeNodes = null;
        root = null;
        level1c = null;
        level1a = null;
        level1b = null;
        level2b = null;
        level2c1 = null;
        level2a1 = null;
        level2a2 = null;
        level2c2 = null;
        level2c3 = null;
    }





    @Test
    public void testToList() {
        List<PTNode> list = new ArrayList<>();
        list.add(root);
        list.add(level1a);
        list.add(level1b);
        list.add(level1c);
        list.add(level2a1);
        list.add(level2a2);
        list.add(level2b);
        list.add(level2c1);
        list.add(level2c2);
        list.add(level2c3);
        ParseTree parseTree = MetaParseTree.generateTree(treeNodes);
        List<PTNode> nodes = parseTree.toList();
        Assertions.assertTrue(nodes.containsAll(list));

    }

    @Test
    public void testAdd() {
        treeNodes.remove(level2b);
        ParseTree parseTree = MetaParseTree.generateTree(treeNodes);
        parseTree.add(level2b);
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1a));
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1b));
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1c));
        Assertions.assertTrue(level1a.getNextNodes().contains(level2a1));
        Assertions.assertTrue(level1a.getNextNodes().contains(level2a2));
        Assertions.assertTrue(level1b.getNextNodes().contains(level2b));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c1));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c2));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c3));
    }

    @Test
    public void testGetSubTree() {
        ParseTree parseTree = MetaParseTree.generateTree(treeNodes);
        List<ParseTree> subTree = parseTree.getSubTree(root.getEntityClass(), root.getEntityField());
        Assertions.assertTrue(subTree.size() == 3);
        Assertions.assertTrue(subTree.get(0).root().equals(level1a));
        Assertions.assertTrue(subTree.get(1).root().equals(level1b));
        Assertions.assertTrue(subTree.get(2).root().equals(level1c));
    }

    @Test
    public void testGenerateTree() {
        ParseTree parseTree = MetaParseTree.generateTree(treeNodes);
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1a));
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1b));
        Assertions.assertTrue(parseTree.root().getNextNodes().contains(level1c));
        Assertions.assertTrue(level1a.getNextNodes().contains(level2a1));
        Assertions.assertTrue(level1a.getNextNodes().contains(level2a2));
        Assertions.assertTrue(level1b.getNextNodes().contains(level2b));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c1));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c2));
        Assertions.assertTrue(level1c.getNextNodes().contains(level2c3));
    }

    @Test
    public void testGenerateMultipleTrees() {
        List<IEntityField> fields = new ArrayList<>();
        List<IEntityClass> entityClasses = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            fields.add(EntityField.Builder.anEntityField().withId(i - 10).build());
            entityClasses.add(EntityClass.Builder.anEntityClass().withId(i).withField(fields.get(i - 10)).build());
        }
        PTNode treeRoot1 = new PTNode();
        treeRoot1.setEntityField(fields.get(0));
        treeRoot1.setEntityClass(entityClasses.get(0));
        EntityField build = EntityField.Builder.anEntityField().withId(100).build();
        treeRoot1.setAggEntityClass(EntityClass.Builder.anEntityClass().withId(100).withField(build).build());
        treeRoot1.setAggEntityField(build);
        treeRoot1.setRootFlag(true);
        treeNodes.add(treeRoot1);

        PTNode tree1Node1 = new PTNode();
        tree1Node1.setEntityField(fields.get(1));
        tree1Node1.setEntityClass(entityClasses.get(1));
        tree1Node1.setAggEntityClass(treeRoot1.getEntityClass());
        tree1Node1.setAggEntityField(treeRoot1.getEntityField());
        treeNodes.add(tree1Node1);

        PTNode treeRoot2 = new PTNode();
        treeRoot2.setEntityClass(entityClasses.get(2));
        treeRoot2.setEntityField(fields.get(2));
        treeRoot2.setAggEntityClass(EntityClass.Builder.anEntityClass().withId(100).withField(build).build());
        treeRoot2.setAggEntityField(build);
        treeRoot2.setRootFlag(true);
        treeNodes.add(treeRoot2);


        PTNode tree2Node2 = new PTNode();
        tree2Node2.setEntityField(fields.get(3));
        tree2Node2.setEntityClass(entityClasses.get(3));
        tree2Node2.setAggEntityClass(treeRoot2.getEntityClass());
        tree2Node2.setAggEntityField(treeRoot2.getEntityField());
        treeNodes.add(tree2Node2);


        PTNode tree2Node3 = new PTNode();
        tree2Node3.setEntityField(fields.get(4));
        tree2Node3.setEntityClass(entityClasses.get(4));
        tree2Node3.setAggEntityClass(treeRoot2.getEntityClass());
        tree2Node3.setAggEntityField(treeRoot2.getEntityField());
        treeNodes.add(tree2Node3);

        PTNode tree2Node4 = new PTNode();
        tree2Node4.setEntityField(fields.get(5));
        tree2Node4.setEntityClass(entityClasses.get(5));
        tree2Node4.setAggEntityClass(tree2Node3.getEntityClass());
        tree2Node4.setAggEntityField(tree2Node3.getEntityField());
        treeNodes.add(tree2Node4);

        PTNode tree2Node5 = new PTNode();
        tree2Node5.setEntityField(fields.get(6));
        tree2Node5.setEntityClass(entityClasses.get(6));
        tree2Node5.setAggEntityClass(tree2Node3.getEntityClass());
        tree2Node5.setAggEntityField(tree2Node3.getEntityField());
        treeNodes.add(tree2Node5);

        PTNode tree2Node6 = new PTNode();
        tree2Node6.setEntityField(fields.get(7));
        tree2Node6.setEntityClass(entityClasses.get(7));
        tree2Node6.setAggEntityClass(tree2Node5.getEntityClass());
        tree2Node6.setAggEntityField(tree2Node5.getEntityField());
        treeNodes.add(tree2Node6);


        PTNode tree2Node7 = new PTNode();
        tree2Node7.setEntityField(fields.get(8));
        tree2Node7.setEntityClass(entityClasses.get(8));
        tree2Node7.setAggEntityClass(tree2Node6.getEntityClass());
        tree2Node7.setAggEntityField(tree2Node6.getEntityField());
        treeNodes.add(tree2Node7);

        PTNode tree2Node8 = new PTNode();
        tree2Node8.setEntityField(fields.get(9));
        tree2Node8.setEntityClass(entityClasses.get(9));
        tree2Node8.setAggEntityClass(tree2Node7.getEntityClass());
        tree2Node8.setAggEntityField(tree2Node7.getEntityField());
        treeNodes.add(tree2Node8);

        List<ParseTree> trees = MetaParseTree.generateMultipleTress(treeNodes);
        Assertions.assertTrue(trees.size() == 3);


    }

    @Test
    public void testGetLevelList() {
        ParseTree parseTree = MetaParseTree.generateTree(treeNodes);
        List<List<PTNode>> levelList = parseTree.getLevelList();
        Assertions.assertTrue(levelList.size() == 3);
        Assertions.assertTrue(levelList.get(0).size() == 1 && levelList.get(0).contains(root));
        List<PTNode> list = new ArrayList<>();
        list.add(level1a);
        list.add(level1b);
        list.add(level1c);
        Assertions.assertTrue(levelList.get(1).size() == 3 && levelList.get(1).containsAll(list));

        list.clear();
        list.add(level2a1);
        list.add(level2a2);
        list.add(level2b);
        list.add(level2c1);
        list.add(level2c2);
        list.add(level2c3);
        Assertions.assertTrue(levelList.get(2).size() == 6 && levelList.get(2).containsAll(list));
    }
}