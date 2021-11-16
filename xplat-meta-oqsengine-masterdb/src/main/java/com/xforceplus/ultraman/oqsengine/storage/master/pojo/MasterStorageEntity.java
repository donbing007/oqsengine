package com.xforceplus.ultraman.oqsengine.storage.master.pojo;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 储存定义.
 */
public class MasterStorageEntity implements Serializable {
    private long id;
    private long tx;
    private long commitid;
    private long createTime;
    private long updateTime;
    private int oqsMajor;
    private int entityClassVersion;
    private int version;
    private int op;
    private boolean deleted;
    private long[] entityClasses;
    private String profile;
    private String attribute;

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

    public String getProfile() {
        return profile;
    }

    /**
     * 获得当前实例实际类型.
     *
     * @return 实际类型标识.
     */
    public long getSelfEntityClassId() {
        for (int i = 0; i < this.entityClasses.length; i++) {
            if (this.entityClasses[i] == 0) {
                if (i > 0) {
                    return this.entityClasses[i - 1];
                } else {
                    return 0;
                }
            }
        }

        return this.entityClasses[this.entityClasses.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MasterStorageEntity)) {
            return false;
        }
        MasterStorageEntity that = (MasterStorageEntity) o;
        return getId() == that.getId()
            && getEntityClassVersion() == that.getEntityClassVersion()
            && getTx() == that.getTx()
            && getCommitid() == that.getCommitid()
            && getVersion() == that.getVersion()
            && getOp() == that.getOp()
            && isDeleted() == that.isDeleted()
            && getCreateTime() == that.getCreateTime()
            && getUpdateTime() == that.getUpdateTime()
            && getOqsMajor() == that.getOqsMajor()
            && Arrays.equals(getEntityClasses(), that.getEntityClasses())
            && getAttribute().equals(that.getAttribute());
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

    /**
     * builder.
     */
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
        private String profile;

        private Builder() {
        }

        public static Builder anStorageEntity() {
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

        public Builder withProfile(String profile) {
            this.profile = profile;
            return this;
        }

        /**
         * 构造MasterStorageEntity实例.
         */
        public MasterStorageEntity build() {
            MasterStorageEntity masterStorageEntity = new MasterStorageEntity();
            masterStorageEntity.updateTime = this.updateTime;
            masterStorageEntity.deleted = this.deleted;
            masterStorageEntity.op = this.op;
            masterStorageEntity.version = this.version;
            masterStorageEntity.entityClassVersion = this.entityClassVersion;
            masterStorageEntity.id = this.id;
            masterStorageEntity.tx = this.tx;
            masterStorageEntity.commitid = this.commitid;
            masterStorageEntity.createTime = this.createTime;
            masterStorageEntity.oqsMajor = this.oqsMajor;
            masterStorageEntity.attribute = this.attribute;
            masterStorageEntity.entityClasses = this.entityClasses;
            masterStorageEntity.profile = (null == this.profile) ? OqsProfile.UN_DEFINE_PROFILE : this.profile;
            return masterStorageEntity;
        }
    }
}
