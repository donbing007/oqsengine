package com.xforceplus.ultraman.oqsengine.storage.pojo;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表示一个数据的原始形态.
 * 不依赖任何实现的一种对象原始数据实体.
 * 可以用以在不同的storage中通信.
 * 这是一个抽像表示.
 * 其内部使用了一个数组来表示属性键值对,即[key,value, key, value...],所以一定是偶数长度.
 *
 * @author dongbin
 * @version 0.1 2021/3/2 13:40
 * @since 1.8
 */
public class OriginalEntity implements Serializable, Cloneable, Comparable<OriginalEntity> {
    private static final IEntityClass ANY_ENTITYCLASS = AnyEntityClass.getInstance();
    private static final Map<String, Object> EMTPY_ATTRIBUTES = Collections.emptyMap();

    private boolean deleted;
    private int op;
    private int version;
    private int oqsMajor;
    private long id;
    private long createTime;
    private long updateTime;
    private long tx;
    private long commitid;
    private IEntityClass entityClass;
    private Map<String, Object> attributes;
    private long maintainid;
    private EntityClassRef entityClassRef;

    /**
     * 实例.
     */
    public OriginalEntity() {
        deleted = false;
        op = OperationType.UNKNOWN.getValue();
        version = 0;
        oqsMajor = OqsVersion.MAJOR;
        id = 0;
        createTime = 0;
        updateTime = 0;
        tx = 0;
        commitid = 0;
        maintainid = 0;
        entityClass = ANY_ENTITYCLASS;
        attributes = EMTPY_ATTRIBUTES;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getOp() {
        return op;
    }

    public int getVersion() {
        return version;
    }

    public int getOqsMajor() {
        return oqsMajor;
    }

    public long getId() {
        return id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getTx() {
        return tx;
    }

    public long getCommitid() {
        return commitid;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOqsMajor(int oqsMajor) {
        this.oqsMajor = oqsMajor;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public void setCommitid(long commitid) {
        this.commitid = commitid;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public void setMaintainid(long maintainid) {
        this.maintainid = maintainid;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public long getMaintainid() {
        return maintainid;
    }

    public int attributeSize() {
        return this.attributes.size();
    }

    public EntityClassRef getEntityClassRef() {
        return entityClassRef;
    }

    public void setEntityClassRef(EntityClassRef entityClassRef) {
        this.entityClassRef = entityClassRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OriginalEntity that = (OriginalEntity) o;
        return deleted == that.deleted && op == that.op && version == that.version && oqsMajor == that.oqsMajor
            && id == that.id && createTime == that.createTime && updateTime == that.updateTime && tx == that.tx
            && commitid == that.commitid && maintainid == that.maintainid && Objects.equals(entityClass,
            that.entityClass) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deleted, op, version, oqsMajor, id, createTime, updateTime, tx, commitid, entityClass,
            attributes, maintainid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OriginalEntity{");
        sb.append("deleted=").append(deleted);
        sb.append(", op=").append(op);
        sb.append(", version=").append(version);
        sb.append(", oqsMajor=").append(oqsMajor);
        sb.append(", id=").append(id);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", tx=").append(tx);
        sb.append(", commitid=").append(commitid);
        sb.append(", entityClass=").append(entityClass);
        sb.append(", attributes=").append(attributes);
        sb.append(", maintainid=").append(maintainid);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return OriginalEntity.Builder.anOriginalEntity()
            .withId(id)
            .withEntityClass(entityClass)
            .withAttributes(new HashMap<>(attributes))
            .withOqsMajor(oqsMajor)
            .withDeleted(deleted)
            .withTx(tx)
            .withCommitid(commitid)
            .withUpdateTime(updateTime)
            .withCreateTime(createTime)
            .withOp(op)
            .withVersion(version);
    }

    @Override
    public int compareTo(OriginalEntity o) {
        if (this.getId() < o.getId()) {
            return -1;
        } else if (this.getId() > o.getId()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private boolean deleted;
        private int op;
        private int version;
        private int oqsMajor;
        private long id;
        private long createTime;
        private long updateTime;
        private long tx;
        private long commitid;
        private IEntityClass entityClass = ANY_ENTITYCLASS;
        private Map<String, Object> attributes = Collections.emptyMap();
        private long maintainid;
        private EntityClassRef entityClassRef;

        public static Builder anOriginalEntity() {
            return new Builder();
        }

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder withOp(int op) {
            this.op = op;
            return this;
        }

        public Builder withMaintainid(long maintainid) {
            this.maintainid = maintainid;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withOqsMajor(int oqsMajor) {
            this.oqsMajor = oqsMajor;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withCreateTime(long createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withTx(long tx) {
            this.tx = tx;
            return this;
        }

        public Builder withCommitid(long commitid) {
            this.commitid = commitid;
            return this;
        }

        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withEntityClassRef(EntityClassRef entityClassRef) {
            this.entityClassRef = entityClassRef;
            return this;
        }

        /**
         * 属性集合.
         */
        public Builder withAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        /**
         * 属性.
         */
        public Builder withAttribute(String key, Object value) {
            if (Collections.emptyMap().getClass().equals(this.attributes.getClass())) {
                this.attributes = new HashMap<>();
            }
            this.attributes.put(key, value);
            return this;
        }

        /**
         * 构造实例.
         */
        public OriginalEntity build() {
            OriginalEntity originalEntity = new OriginalEntity();
            originalEntity.oqsMajor = this.oqsMajor;
            originalEntity.commitid = this.commitid;
            originalEntity.deleted = this.deleted;
            originalEntity.updateTime = this.updateTime;
            originalEntity.createTime = this.createTime;
            originalEntity.entityClass = this.entityClass;
            originalEntity.op = this.op;
            originalEntity.id = this.id;
            originalEntity.attributes = this.attributes;
            originalEntity.version = this.version;
            originalEntity.tx = this.tx;
            originalEntity.entityClassRef = this.entityClassRef;
            originalEntity.maintainid = this.maintainid;

            return originalEntity;
        }
    }
}
