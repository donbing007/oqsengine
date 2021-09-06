package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import java.util.ArrayList;
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
     * 版本信息.
     */
    private int version;


    /**
     * 节点对象信息.
     */
    private IEntityClass entityClass;

    /**
     * 节点字段信息.
     */
    private IEntityField entityField;

    /**
     * 被聚合节点对象信息.
     */
    private IEntityClass aggEntityClass;


    /**
     * 被聚合节点字段信息.
     */
    private IEntityField aggEntityField;

    /**
     * 节点条件信息.
     */
    private List<Condition> conditions;

    /**
     * 关系信息.
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

    private AggregationType aggregationType;

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public boolean isRootFlag() {
        return rootFlag;
    }

    public void setRootFlag(boolean rootFlag) {
        this.rootFlag = rootFlag;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public IEntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(IEntityField entityField) {
        this.entityField = entityField;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<PTNode> getNextNodes() {
        return nextNodes == null ? new ArrayList<>() : nextNodes;
    }

    public void setNextNodes(List<PTNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public PTNode getPreNode() {
        return preNode;
    }

    public void setPreNode(PTNode preNode) {
        this.preNode = preNode;
    }

    public IEntityClass getAggEntityClass() {
        return aggEntityClass;
    }

    public void setAggEntityClass(IEntityClass aggEntityClass) {
        this.aggEntityClass = aggEntityClass;
    }

    public IEntityField getAggEntityField() {
        return aggEntityField;
    }

    public void setAggEntityField(IEntityField aggEntityField) {
        this.aggEntityField = aggEntityField;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public PTNode() {
    }
}
