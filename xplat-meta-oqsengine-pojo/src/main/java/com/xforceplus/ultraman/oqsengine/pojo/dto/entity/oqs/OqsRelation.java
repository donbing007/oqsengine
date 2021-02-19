package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

import java.util.Objects;

/**
 * desc :
 * name : OqsRelation
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class OqsRelation {

    private Long id;

    /**
     * 关系名称 - 目前采用关系ID来填入
     */
    private String name;

    /**
     * 关联对象Id
     */
    private long entityClassId;

    private String entityClassName;

    private long relOwnerClassId;

    private String relOwnerClassName;

    /**
     * 关系类型 - 使用关系的code填入
     */
    private String relationType;

    /**
     * 是否使用主键作为关系字段 - true代表是。- 目前只支持主键模式
     * false表示使用对象的其他唯一属性来定义
     */
    private boolean identity;

    /**
     * 关系表示使用
     * Key字段的id使用关系的id值
     * 关系类型默认都是long
     */
    private long entityFieldId;

    public OqsRelation() {
    }

    public OqsRelation(String name, long entityClassId, String relationType, boolean identity, long entityFieldId) {
        this.name = name;
        this.entityClassId = entityClassId;
        this.relationType = relationType;
        this.identity = identity;
        this.entityFieldId = entityFieldId;
    }

    public OqsRelation(Long id, String name, long entityClassId, String entityClassName, String ownerClassName, String relationType) {
        this.name = name;
        this.entityClassName = entityClassName;
        this.relOwnerClassName = ownerClassName;
        this.relationType = relationType;
        this.id = id;
        this.entityClassId = entityClassId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public long getRelOwnerClassId() {
        return relOwnerClassId;
    }

    public void setRelOwnerClassId(long relOwnerClassId) {
        this.relOwnerClassId = relOwnerClassId;
    }

    public String getRelOwnerClassName() {
        return relOwnerClassName;
    }

    public void setRelOwnerClassName(String relOwnerClassName) {
        this.relOwnerClassName = relOwnerClassName;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public long getEntityFieldId() {
        return entityFieldId;
    }

    public void setEntityFieldId(long entityFieldId) {
        this.entityFieldId = entityFieldId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OqsRelation)) return false;
        OqsRelation relation = (OqsRelation) o;
        return getEntityClassId() == relation.getEntityClassId() &&
                isIdentity() == relation.isIdentity() &&
                Objects.equals(getName(), relation.getName()) &&
                Objects.equals(getRelationType(), relation.getRelationType()) &&
                getEntityFieldId() == relation.getEntityFieldId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEntityClassId(), getRelationType(), isIdentity(), getEntityFieldId());
    }

    @Override
    public String toString() {
        return "Relation{" +
                "name='" + name + '\'' +
                ", entityClassId=" + entityClassId +
                ", relationType='" + relationType + '\'' +
                ", identity=" + identity +
                ", entityField=" + entityFieldId +
                '}';
    }
}
