package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解析树节点.
 *
 * @className: PTNode
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl
 * @author: wangzheng
 * @date: 2021/8/30 14:49
 */
public class PTNode implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(PTNode.class);
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
    private Conditions conditions;

    /**
     * 关系信息.
     */
    private Relationship relationship;

    /**
     * 子节点.
     */
    private List<PTNode> nextNodes;

    /**
     * 节点对象ref.
     */
    private EntityClassRef entityClassRef;

    /**
     * 被聚合节点对象ref.
     */
    private EntityClassRef aggEntityClassRef;

    /**
     * 节点字段id.
     */
    private long entityFieldId;

    /**
     * 被聚合节点字段id.
     */
    private long aggEntityFieldId;

    /**
     * 关系id.
     */
    private long relationId;

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

    /**
     * emptyConditions处理.
     */
    public Conditions getConditions() {
        if (conditions == null) {
            conditions = Conditions.buildEmtpyConditions();
        }
        return conditions;
    }

    public void setConditions(Conditions conditions) {
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
        nextNodes = new ArrayList<>();
    }

    public long getRelationId() {
        return relationId;
    }

    public void setRelationId(long relationId) {
        this.relationId = relationId;
    }

    public EntityClassRef getEntityClassRef() {
        return entityClassRef;
    }

    public void setEntityClassRef(EntityClassRef entityClassRef) {
        this.entityClassRef = entityClassRef;
    }

    public EntityClassRef getAggEntityClassRef() {
        return aggEntityClassRef;
    }

    public void setAggEntityClassRef(EntityClassRef aggEntityClassRef) {
        this.aggEntityClassRef = aggEntityClassRef;
    }

    public long getEntityFieldId() {
        return entityFieldId;
    }

    public void setEntityFieldId(long entityFieldId) {
        this.entityFieldId = entityFieldId;
    }

    public long getAggEntityFieldId() {
        return aggEntityFieldId;
    }

    public void setAggEntityFieldId(long aggEntityFieldId) {
        this.aggEntityFieldId = aggEntityFieldId;
    }

    /**
     * checkNode成功后才可以进行转换.
     */
    public PTNode toSimpleNode() {
        this.entityClassRef = entityClass.ref();
        this.entityClass = null;
        this.aggEntityClassRef = aggEntityClass.ref();
        this.aggEntityClass = null;
        this.entityFieldId = entityField.id();
        this.entityField = null;
        this.aggEntityFieldId = aggEntityField.id();
        this.aggEntityField = null;
        this.relationId = relationship.getId();
        this.relationship = null;
        return this;
    }

    /**
     * 检查必要属性不为空.
     */
    public static boolean checkNode(PTNode node) {
        if (node.getEntityClass() == null) {
            logger.error("===============entityclass info can not be empty");
            return false;
        } else if (node.getEntityField() == null) {
            logger.error("================entityfield info can not be empty");
            return false;
        } else if (node.getAggEntityClass() == null) {
            logger.error("===============aggEntityClass info can not be empty");
            return false;
        } else if (node.getAggEntityField() == null) {
            logger.error("=====================aggEntityField info can not be empty");
            return false;
        } else if (node.getRelationship() == null) {
            logger.error("=========================relationship info can not be empty");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PTNode{" + "rootFlag=" + rootFlag + ", level=" + level + ", version=" + version + ", entityClass=" + entityClass + ", entityField=" + entityField + ", aggEntityClass=" + aggEntityClass + ", aggEntityField=" + aggEntityField + ", conditions=" + conditions + ", relationship=" + relationship + ", nextNodes=" + nextNodes + ", preNode=" + preNode + ", aggregationType=" + aggregationType + '}';
    }
}
