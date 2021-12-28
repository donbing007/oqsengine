package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Entity实体定义.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public class Entity implements IEntity, Serializable {

    /*
    判断是否已经成功调用过delete方法.
     */
    private boolean deleted;
    /*
     * 数据版本
     */
    private int version;
    /*
     * 产生数据的oqs版本.
     */
    private int major;
    /*
     * 数据id
     */
    private long id;
    /*
     * 时间
     */
    private long time;
    /*
     * 维护标识.
     */
    private long maintainid;
    /*
     * 数据结构
     */
    private EntityClassRef entityClassRef;
    /*
     * 数据集合
     */
    private IEntityValue entityValue;

    @Override
    public long id() {
        return id;
    }

    @Override
    public EntityClassRef entityClassRef() {
        return entityClassRef;
    }

    @Override
    public IEntityValue  entityValue() {
        if (this.entityValue == null) {
            this.entityValue = new EntityValue(this);
        }

        return this.entityValue;
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
    public void markTime() {
        this.time = System.currentTimeMillis();
    }

    @Override
    public long maintainId() {
        return maintainid;
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
        Entity cloneEntity = new Entity();
        cloneEntity.id = this.id;
        cloneEntity.entityClassRef = this.entityClassRef;

        cloneEntity.entityValue = new EntityValue(cloneEntity);
        cloneEntity.entityValue.addValues(this.entityValue.values());

        cloneEntity.version = this.version;
        cloneEntity.major = this.major;
        cloneEntity.time = this.time;
        cloneEntity.maintainid = this.maintainid;

        return cloneEntity;
    }

    @Override
    public void restMaintainId(long maintainId) {
        this.maintainid = maintainId;
    }

    @Override
    public void delete() {
        this.deleted = true;
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
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
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private long id;
        private long time;
        private EntityClassRef entityClassRef;
        private Collection<IValue> values;
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

        /**
         * 构造时指定字段值.
         *
         * @param values 值列表.
         * @return 构造器.
         */
        public Builder withValues(Collection<IValue> values) {
            if (this.values == null) {
                this.values = new ArrayList<>();
            }

            this.values.addAll(values);
            return this;
        }

        /**
         * 构造时指定字段值.
         *
         * @param value 单个字段值.
         * @return 构造器.
         */
        public Builder withValue(IValue value) {
            if (this.values == null) {
                this.values = new ArrayList<>();
            }

            this.values.add(value);
            return this;
        }

        /**
         * 构造Entity实例.
         *
         * @return 实例.
         */
        public Entity build() {
            Entity entity = new Entity();
            entity.maintainid = this.maintainid;
            entity.version = this.version;
            entity.major = this.major;
            entity.time = this.time;
            entity.entityClassRef = this.entityClassRef;
            entity.id = this.id;
            if (this.values != null) {
                entity.entityValue().addValues(values);
            }
            return entity;
        }
    }
}
