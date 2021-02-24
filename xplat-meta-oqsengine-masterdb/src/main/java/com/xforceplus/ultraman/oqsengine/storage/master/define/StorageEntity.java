package com.xforceplus.ultraman.oqsengine.storage.master.define;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 储存定义.
 */
public class StorageEntity implements Serializable {
    private long id;
    private long[] entityClasses;
    private int entityClassVersion;
    private long tx;
    private long commitid;
    private int version;
    private int op;
    private boolean deleted;
    private String attribute;
    private long createTime;
    private long updateTime;
    private int oqsMajor;

    public long getId() {
        return id;
    }

    public long[] getEntityClasses() {
        return entityClasses;
    }

    public int getEntityClassVersion() {
        return entityClassVersion;
    }

    public long getTx() {
        return tx;
    }

    public long getCommitid() {
        return commitid;
    }

    public int getVersion() {
        return version;
    }

    public int getOp() {
        return op;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getAttribute() {
        return attribute;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public int getOqsMajor() {
        return oqsMajor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageEntity)) {
            return false;
        }
        StorageEntity that = (StorageEntity) o;
        return getId() == that.getId() &&
            getEntityClassVersion() == that.getEntityClassVersion() &&
            getTx() == that.getTx() &&
            getCommitid() == that.getCommitid() &&
            getVersion() == that.getVersion() &&
            getOp() == that.getOp() &&
            isDeleted() == that.isDeleted() &&
            getCreateTime() == that.getCreateTime() &&
            getUpdateTime() == that.getUpdateTime() &&
            getOqsMajor() == that.getOqsMajor() &&
            Arrays.equals(getEntityClasses(), that.getEntityClasses()) &&
            getAttribute().equals(that.getAttribute());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
            getId(),
            getEntityClassVersion(),
            getTx(),
            getCommitid(),
            getVersion(),
            getOp(),
            isDeleted(),
            getAttribute(),
            getCreateTime(),
            getUpdateTime(),
            getOqsMajor());
        result = 31 * result + Arrays.hashCode(getEntityClasses());
        return result;
    }


    public static final class Builder {
        private long id;
        private long[] entityClasses;
        private int entityClassVersion;
        private long tx;
        private long commitid;
        private int version;
        private int op;
        private boolean deleted;
        private String attribute;
        private long createTime;
        private long updateTime;
        private int oqsMajor;

        private Builder() {
        }

        public static Builder aStorageEntity() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withEntityClasses(long[] entityClasses) {
            this.entityClasses = entityClasses;
            return this;
        }

        public Builder withEntityClassVersion(int entityClassVersion) {
            this.entityClassVersion = entityClassVersion;
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

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withOp(int op) {
            this.op = op;
            return this;
        }

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder withAttribute(String attribute) {
            this.attribute = attribute;
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

        public Builder withOqsMajor(int oqsMajor) {
            this.oqsMajor = oqsMajor;
            return this;
        }

        public StorageEntity build() {
            StorageEntity storageEntity = new StorageEntity();
            storageEntity.updateTime = this.updateTime;
            storageEntity.deleted = this.deleted;
            storageEntity.op = this.op;
            storageEntity.version = this.version;
            storageEntity.entityClassVersion = this.entityClassVersion;
            storageEntity.id = this.id;
            storageEntity.tx = this.tx;
            storageEntity.commitid = this.commitid;
            storageEntity.createTime = this.createTime;
            storageEntity.oqsMajor = this.oqsMajor;
            storageEntity.attribute = this.attribute;
            storageEntity.entityClasses = this.entityClasses;
            return storageEntity;
        }
    }
}
