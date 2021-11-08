package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.io.Serializable;
import java.util.List;

/**
 * 聚合解析树.
 *
 * @className: AggTree
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree
 * @author: wangzheng
 * @date: 2021/8/30 10:34
 */
public interface ParseTree extends Serializable {

    /**
     * 树的根节点，非空.
     *
     * @return 根节点.
     */
    PTNode root();

    int treeLevel();


    /**
     * 将当前树转化成节点集合，层次遍历.
     *
     * @return 树的所有节点的集合.
     */
    List<PTNode> toList();


    /**
     * 添加node到树中， 若无依赖关系添加失败.
     *
     * @param node 指定节点.
     */
    void add(PTNode node);

    /**
     * 替换树中节点信息.
     *
     * @param node 指定节点.
     */
    void replace(PTNode node);


    /**
     * 根据被聚合entityClass和entityField信息查找子树.
     *
     * @param entityClass  被聚合entityClass.
     * @param entityField  被聚合entityField.
     * @return 子树列表.
     */
    List<ParseTree> getSubTree(IEntityClass entityClass, IEntityField entityField);



    /**
     * 获取树的所有层级的node集合.
     *
     * @return 层级node集合.
     */
    public List<List<PTNode>> getLevelList();

    /**
     * 根据根节点构建tree.
     *
     * @param entityClasses 对象集合.
     * @param rootEntityClass 根对象.
     * @param rootEntityField 根字段.
     * @param aggEntityClass 被聚合对象.
     * @param aggEntityField 被聚合字段.
     * @return ParseTree.
     */
    ParseTree buildTree(List<IEntityClass> entityClasses, IEntityClass rootEntityClass, IEntityField rootEntityField,
                        IEntityClass aggEntityClass, IEntityField aggEntityField);

}
