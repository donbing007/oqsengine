package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entity实体定义.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public class Entity implements IEntity, Serializable {

    private static final IEntityFamily EMPTY_FAMILY = new EntityFamily(0, 0);

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
    public IEntityClass entityClass() {
        return entityClass;
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
    public IEntityFamily family() {
        return family;
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

    @Deprecated
    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue) {
        this(id, entityClass, entityValue, null, 0, OqsVersion.MAJOR);
    }

    @Deprecated
    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int major) {
        this(id, entityClass, entityValue, null, 0, major);
    }

    @Deprecated
    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int version, int major) {
        this(id, entityClass, entityValue, null, version, major);
    }

    @Deprecated
    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, IEntityFamily family, int version, int major) {
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
        this.major = major;
    }

    /**
     * 重置 id 为新的 id.
     *
     * @param id 新的 id.
     */
    @Override
    public void resetId(long id) {
        this.id = id;
        this.entityValue.restId(id);
    }

    @Override
    public void resetFamily(IEntityFamily family) {
        this.family = family;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return Entity.Builder.anEntity()
            .withId(id())
            .withEntityClass(entityClass())
            .withEntityValue((IEntityValue) entityValue().clone())
            .withFamily(family())
            .withVersion(version())
            .withTime(time())
            .withMajor(OqsVersion.MAJOR).build();
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
        if (id != entity.id) {
            return false;
        }
        if (version != entity.version) {
            return false;
        }
        if (time != entity.time) {
            return false;
        }
        if (major != entity.major) {
            return false;
        }
        if (!entityClass.equals(entity.entityClass)) {
            return false;
        }
        if (!entityValue.equals(entity.entityValue)) {
            return false;
        }
        if (!family.equals(entity.family)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, entityClass, entityValue, family, version, major);
    }

    @Override
    public String toString() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder buff = new StringBuilder();
        buff.append("id: ").append(id)
            .append(", ")
            .append("entity: ").append(entityClass.code())
            .append(", ")
            .append("version: ").append(version)
            .append(", ")
            .append("time: ").append(
            df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())))
            .append(", ")
            .append("major: ").append(major)
            .append(", ")
            .append("pref: ").append(family.parent())
            .append(", ")
            .append("cref: ").append(family.child())
            .append("\n");
        entityValue().values().stream().forEach(v -> {
            buff.append("{")
                .append("id: ").append(v.getField().id())
                .append(", ")
                .append("name: ").append(v.getField().name())
                .append(", ")
                .append("type: ").append(v.getField().type().getType())
                .append(", ")
                .append("value: ").append(v.getValue().toString())
                .append("}\n");
        });

        return buff.toString();
    }


    public static final class Builder {
        private long id;
        private long time;
        private IEntityClass entityClass;
        private IEntityValue entityValue;
        private IEntityFamily family = EMPTY_FAMILY;
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

        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withEntityValue(IEntityValue entityValue) {
            this.entityValue = entityValue;
            return this;
        }

        public Builder withFamily(IEntityFamily family) {
            this.family = family;
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
            entity.entityClass = this.entityClass;
            entity.version = this.version;
            entity.time = this.time;
            entity.family = this.family;
            entity.id = this.id;
            entity.entityValue = this.entityValue;
            entity.maintainid = this.maintainid;
            entity.major = this.major;
            return entity;
        }
    }
}
