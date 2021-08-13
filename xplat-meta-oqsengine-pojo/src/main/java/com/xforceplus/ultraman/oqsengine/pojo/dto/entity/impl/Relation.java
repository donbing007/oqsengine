package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;

/**
 * 关系对象.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 * @deprecated 已经被废弃.
 */
public class Relation {

    private Long id;

    /*
     * 关系名称 - 目前采用关系ID来填入
     */
    private String name;

    /*
     * 关联对象Id
     */
    private long entityClassId;

    private String entityClassName;

    private long relOwnerClassId;

    private String relOwnerClassName;

    /*
     * 关系类型 - 使用关系的code填入
     */
    private String relationType;

    /*
     * 是否使用主键作为关系字段 - true代表是。- 目前只支持主键模式
     * false表示使用对象的其他唯一属性来定义
     */
    private boolean identity;

    /*
     * 关系表示使用
     * Key字段的id使用关系的id值
     * <p>
     * 关系字段命名 :
     * 情况1：OTO类型下采用子对象的ref+code+id的方式命名
     * 例如：order -> address。在order中该keyName则为ref_address_id
     * 情况2：OTM类型下采用父对象的code+id的方式命名
     * 例如：order -> goods。在goods中该keyName则为ref_order_id
     * <p>
     * 关系类型默认都是long
     */
    private IEntityField entityField;

    public Relation() {
    }

    /**
     * 创建实列.
     */
    public Relation(String name, long entityClassId, String relationType, boolean identity, IEntityField entityField) {
        this.name = name;
        this.entityClassId = entityClassId;
        this.relationType = relationType;
        this.identity = identity;
        this.entityField = entityField;
    }

    /**
     * 创建实列.
     */
    public Relation(Long id, String name, long entityClassId, String entityClassName, String ownerClassName,
                    String relationType) {
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

    public IEntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(IEntityField entityField) {
        this.entityField = entityField;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relation)) {
            return false;
        }
        Relation relation = (Relation) o;
        return getEntityClassId() == relation.getEntityClassId()
            && isIdentity() == relation.isIdentity()
            && Objects.equals(getName(), relation.getName())
            && Objects.equals(getRelationType(), relation.getRelationType())
            && Objects.equals(getEntityField(), relation.getEntityField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEntityClassId(), getRelationType(), isIdentity(), getEntityField());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Relation{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", entityClassId=").append(entityClassId);
        sb.append(", entityClassName='").append(entityClassName).append('\'');
        sb.append(", relOwnerClassId=").append(relOwnerClassId);
        sb.append(", relOwnerClassName='").append(relOwnerClassName).append('\'');
        sb.append(", relationType='").append(relationType).append('\'');
        sb.append(", identity=").append(identity);
        sb.append(", entityField=").append(entityField);
        sb.append('}');
        return sb.toString();
    }
}
