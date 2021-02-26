package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity实体定义.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public class Entity implements IEntity, Serializable {

    /**
     * 数据id
     */
    private long id;
    /**
     * 时间
     */
    private long time;
    /**
     * 数据结构
     */
    private EntityClassRef entityClassRef;
    /**
     * 数据集合
     */
    private IEntityValue entityValue;

    /**
     * 数据版本
     */
    private int version;

    /**
     * 维护标识.
     */
    private long maintainid;

    /**
     * 产生数据的oqs版本.
     */
    private int major;

    @Override
    public long id() {
        return id;
    }

    @Override
    public EntityClassRef entityClassRef() {
        return entityClassRef;
    }

    @Override
    public IEntityValue entityValue() {
        return entityValue;
    }

    @Override
    public void resetEntityValue(IEntityValue iEntityValue) {
        this.entityValue = iEntityValue;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public void resetVersion(int version) {
        this.version = version;
    }

    @Override
    public long time() {
        return time;
    }

    @Override
    public void markTime(long time) {
        this.time = time;
    }

    @Override
    public long maintainId() {
        return maintainid;
    }


    @Override
    public void markTime() {
        this.time = System.currentTimeMillis();
    }

    @Override
    public int major() {
        return this.major;
    }

    public Entity() {
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
    public Object clone() throws CloneNotSupportedException {
        return Entity.Builder.anEntity()
            .withId(id)
            .withEntityClassRef(entityClassRef)
            .withEntityValue((IEntityValue) entityValue().clone())
            .withVersion(version)
            .withTime(time)
            .withMajor(major).build();
    }

    @Override
    public void restMaintainId(long maintainId) {
        this.maintainid = maintainId;
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
            time == entity.time &&
            version == entity.version &&
            maintainid == entity.maintainid &&
            major == entity.major &&
            entityClassRef.equals(entity.entityClassRef) &&
            entityValue.equals(entity.entityValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, entityClassRef, entityValue, version, maintainid, major);
    }

    /**
     * Builder
     */
    public static final class Builder {
        private long id;
        private long time;
        private EntityClassRef entityClassRef;
        private IEntityValue entityValue;
        private int version;
        private long maintainid;
        private int major;

        private Builder() {
        }

        public static Builder anEntity() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withTime(long time) {
            this.time = time;
            return this;
        }

        public Builder withEntityClassRef(EntityClassRef entityClassRef) {
            this.entityClassRef = entityClassRef;
            return this;
        }

        public Builder withEntityValue(IEntityValue entityValue) {
            this.entityValue = entityValue;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withMaintainid(long maintainid) {
            this.maintainid = maintainid;
            return this;
        }

        public Builder withMajor(int major) {
            this.major = major;
            return this;
        }

        public Entity build() {
            Entity entity = new Entity();
            entity.maintainid = this.maintainid;
            entity.version = this.version;
            entity.major = this.major;
            entity.time = this.time;
            entity.entityValue = this.entityValue;
            entity.entityClassRef = this.entityClassRef;
            entity.id = this.id;
            return entity;
        }
    }
}
