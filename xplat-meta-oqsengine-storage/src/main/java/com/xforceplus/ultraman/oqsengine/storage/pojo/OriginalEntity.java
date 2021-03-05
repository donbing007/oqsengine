package com.xforceplus.ultraman.oqsengine.storage.pojo;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;

import java.io.Serializable;
import java.util.*;

/**
 * 表示一个数据的原始形态.
 * 不依赖任何实现的一种对象原始数据实体.
 * 可以用以在不同的storage中通信.
 * 这是一个抽像表示.
 *
 * @author dongbin
 * @version 0.1 2021/3/2 13:40
 * @since 1.8
 */
public class OriginalEntity implements Serializable {
    private static final IEntityClass ANY_ENTITYCLASS = AnyEntityClass.getInstance();
    private static final Object[] EMTPY_ATTRIBUTES = new Object[0];

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
    private Object[] attributes;

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

    public Object[] getAttributes() {
        return attributes;
    }

    public Collection<Map.Entry<String, Object>> listAttributes() {
        final int space = 2;
        List<Map.Entry<String, Object>> attributeList = new ArrayList<>(attributes.length / space);
        for (int i = 0; i < attributes.length; i += space) {
            attributeList.add(new AbstractMap.SimpleEntry(attributes[i], attributes[i + 1]));
        }
        return attributeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OriginalEntity)) {
            return false;
        }
        OriginalEntity that = (OriginalEntity) o;
        return isDeleted() == that.isDeleted() &&
            getOp() == that.getOp() &&
            getVersion() == that.getVersion() &&
            getOqsMajor() == that.getOqsMajor() &&
            getId() == that.getId() &&
            getCreateTime() == that.getCreateTime() &&
            getUpdateTime() == that.getUpdateTime() &&
            getTx() == that.getTx() &&
            getCommitid() == that.getCommitid() &&
            Objects.equals(getEntityClass(), that.getEntityClass()) &&
            Arrays.equals(getAttributes(), that.getAttributes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
            isDeleted(),
            getOp(),
            getVersion(),
            getOqsMajor(),
            getId(),
            getCreateTime(),
            getUpdateTime(),
            getTx(),
            getCommitid(),
            getEntityClass());
        result = 31 * result + Arrays.hashCode(getAttributes());
        return result;
    }


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
        private IEntityClass entityClass;
        private Collection<Object> attributes;

        private Builder() {
            attributes = new LinkedList();
        }

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

        public Builder withAttributes(Collection<Object> attributes) {
            this.attributes.clear();
            this.attributes.addAll(attributes);
            return this;
        }

        public Builder withAttribute(String key, Object value) {
            this.attributes.add(key);
            this.attributes.add(value);
            return this;
        }

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
            originalEntity.attributes = this.attributes.toArray();
            originalEntity.version = this.version;
            originalEntity.tx = this.tx;

            // 预期 attributes一定是偶数的长度,[key,value,key,valye...]方式储存.
            final int space = 2;
            // 必须是偶数.
            if (this.attributes.size() % space != 0) {
                throw new IllegalArgumentException(String.format("Incomplete attributes.[%d].", originalEntity.getId()));
            }

            return originalEntity;
        }
    }
}
