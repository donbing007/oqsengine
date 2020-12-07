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

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue) {
        this(id, entityClass, entityValue, null, 0, OqsVersion.MAJOR);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int major) {
        this(id, entityClass, entityValue, null, 0, major);
    }

    public Entity(long id, IEntityClass entityClass, IEntityValue entityValue, int version, int major) {
        this(id, entityClass, entityValue, null, version, major);
    }

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
        IEntity newEntity = new Entity(
            id(), entityClass(), (IEntityValue) entityValue().clone(), family(), version(), OqsVersion.MAJOR);
        newEntity.markTime(time());
        return newEntity;
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
}
