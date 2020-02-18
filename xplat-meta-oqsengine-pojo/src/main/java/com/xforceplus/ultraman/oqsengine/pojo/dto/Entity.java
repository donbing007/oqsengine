package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;
import com.xforceplus.ultraman.oqsengine.core.metadata.ILink;

import java.util.List;
import java.util.Objects;

public class Entity implements IEntity {

    /**
     * 数据id
     */
    private Long id;
    /**
     * 数据结构
     */
    private IEntityClass entityClass;
    /**
     * 数据集合
     */
    private IEntityValue entityValue;

    /**
     * 值关联信息
     */
    private ILink valueLink;

    /**
     * 继承关联-服务端
     */
    private ILink refLink;

    /**
     * 数据版本
     */
    private String version;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public IEntityClass entityClass() {
        return entityClass;
    }

    @Override
    public IEntityValue entityValue() {
        return entityValue;
    }

    public Entity() {
    }

    public Entity(Long id, IEntityClass entityClass, IEntityValue entityValue, ILink valueLink, ILink refLink, String version) {
        this.id = id;
        this.entityClass = entityClass;
        this.entityValue = entityValue;
        this.valueLink = valueLink;
        this.refLink = refLink;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public IEntityValue getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(IEntityValue entityValue) {
        this.entityValue = entityValue;
    }

    public ILink getValueLink() {
        return valueLink;
    }

    public void setValueLink(ILink valueLink) {
        this.valueLink = valueLink;
    }

    public ILink getRefLink() {
        return refLink;
    }

    public void setRefLink(ILink refLink) {
        this.refLink = refLink;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return Objects.equals(getId(), entity.getId()) &&
                Objects.equals(getEntityClass(), entity.getEntityClass()) &&
                Objects.equals(getEntityValue(), entity.getEntityValue()) &&
                Objects.equals(getValueLink(), entity.getValueLink()) &&
                Objects.equals(getRefLink(), entity.getRefLink()) &&
                Objects.equals(getVersion(), entity.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEntityClass(), getEntityValue(), getValueLink(), getRefLink(), getVersion());
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", entityClass=" + entityClass +
                ", entityValue=" + entityValue +
                ", valueLink=" + valueLink +
                ", refLink=" + refLink +
                ", version='" + version + '\'' +
                '}';
    }
}
