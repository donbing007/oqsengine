package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;

import java.util.Objects;

/**
 * 表示实际 entity 实体.
 */
public class Entity implements IEntity {

    private static final IEntityFamily EMPTY_FAMILY = new EntityFamily(0, 0);

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
    public int version() {
        return version;
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue) {
        this(id, entityClass, entityValue, null, 0);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int version) {
        this(id, entityClass, entityValue, null, version);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, IEntityFamily family, int version) {
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
            Objects.equals(entityValue, entity.entityValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityClass, entityValue, version);
    }

    @Override
    public String toString() {
        return "Entity{" +
            "id=" + id +
            ", entityClass=" + entityClass +
            ", entityValue=" + entityValue +
            ", version=" + version +
            '}';
    }
}
