package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;

import java.util.Objects;

/**
 * 默认情况下,不能进行 Link 查询.
 * 需要指定Link 查询器实现.
 */
public class Entity implements IEntity {

    private static final IEntityFamily EMPTY_FAMILY = new EntityFamily(0, 0);
    private static final IEntityValue EMPTY_REFS = new EntityValue(0);

    /**
     * 数据id
     */
    private long id;
    /**
     * 数据结构
     */
    private IEntityClass entityClass;
    /**
     * 数据集合
     */
    private IEntityValue entityValue;

    /**
     * 继承关系.
     */
    private IEntityFamily family = EMPTY_FAMILY;

    /**
     * 外键集合.
     */
    private IEntityValue refs = EMPTY_REFS;

    /**
     * 数据版本
     */
    private int version;

    @Override
    public long id() {
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

    @Override
    public IEntityFamily family() {
        return null;
    }

    @Override
    public IEntityValue refs() {
        return refs;
    }

    @Override
    public int version() {
        return version;
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue) {
        this(id, entityClass, entityValue, null, null, 0);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int version) {
        this(id, entityClass, entityValue, null, null, version);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, IEntityValue refs, IEntityFamily family, int version) {
        if (entityClass == null) {
            throw new IllegalArgumentException("Invalid class meta information.");
        }

        if (entityValue == null) {
            throw new IllegalArgumentException("Invalid attribute value.");
        }

        this.id = id;
        this.entityClass = entityClass;
        this.entityValue = entityValue;
        if (family != null) {
            this.family = family;
        }
        if (refs != null) {
            this.refs = refs;
        }

        this.version = version;
    }

    /**
     * 重置 id 为新的 id.
     *
     * @param id 新的 id.
     */
    @Override
    public void resetId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entity)) {
            return false;
        }
        Entity entity = (Entity) o;
        return id == entity.id &&
            version == entity.version &&
            Objects.equals(entityClass, entity.entityClass) &&
            Objects.equals(entityValue, entity.entityValue) &&
            Objects.equals(refs, entity.refs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityClass, entityValue, refs, version);
    }

    @Override
    public String toString() {
        return "Entity{" +
            "id=" + id +
            ", entityClass=" + entityClass +
            ", entityValue=" + entityValue +
            ", refs=" + refs +
            ", version=" + version +
            '}';
    }
}
