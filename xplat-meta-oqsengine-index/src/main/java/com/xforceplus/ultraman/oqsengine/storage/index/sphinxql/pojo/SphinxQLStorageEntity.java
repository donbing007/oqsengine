package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo;

import java.io.Serializable;
import java.util.Objects;

/**
 * 索引储存.
 */
public class SphinxQLStorageEntity implements Serializable {
    /**
     * 数据标识.
     */
    private long id;
    /**
     * 全文字段,记录多个entityClass标识.
     */
    private String entityClassF;
    /**
     * 全文字段,记录所有属性.
     */
    private String attributeF;
    /**
     * 全文搜索字段,只会记录字符串属性.
     */
    private String searchAttributeF;
    /**
     * 事务号.
     */
    private long tx;
    /**
     * 提交号.
     */
    private long commitId;
    /**
     * 创建时间时间戳.
     */
    private long createTime;
    /**
     * 最后更新时间戳.
     */
    private long updateTime;
    /**
     * 维护ID.
     */
    private long maintainId;
    /**
     * 数据产生的OQS大版本号.
     */
    private int oqsmajor;
    /**
     * json储存的属性,用以排序过滤.
     */
    private String attribute;

    public SphinxQLStorageEntity() {
    }

    public long getId() {
        return id;
    }

    public String getEntityClassF() {
        return entityClassF;
    }

    public String getAttributeF() {
        return attributeF;
    }

    public long getTx() {
        return tx;
    }

    public long getCommitId() {
        return commitId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getMaintainId() {
        return maintainId;
    }

    public int getOqsmajor() {
        return oqsmajor;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getSearchAttributeF() {
        return searchAttributeF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SphinxQLStorageEntity)) {
            return false;
        }
        SphinxQLStorageEntity that = (SphinxQLStorageEntity) o;
        return getId() == that.getId()
            && getTx() == that.getTx()
            && getCommitId() == that.getCommitId()
            && getCreateTime() == that.getCreateTime()
            && getUpdateTime() == that.getUpdateTime()
            && getMaintainId() == that.getMaintainId()
            && getOqsmajor() == that.getOqsmajor()
            && Objects.equals(getEntityClassF(), that.getEntityClassF())
            && Objects.equals(getAttributeF(), that.getAttributeF())
            && Objects.equals(getAttribute(), that.getAttribute());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getId(),
            getEntityClassF(),
            getAttributeF(),
            getTx(),
            getCommitId(),
            getCreateTime(),
            getUpdateTime(),
            getMaintainId(),
            getOqsmajor(),
            getAttribute());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SphinxQLStorageEntity{");
        sb.append("id=").append(id);
        sb.append(", entityClassF='").append(entityClassF).append('\'');
        sb.append(", attributeF='").append(attributeF).append('\'');
        sb.append(", searchAttributeF='").append(searchAttributeF).append('\'');
        sb.append(", tx=").append(tx);
        sb.append(", commitId=").append(commitId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", maintainId=").append(maintainId);
        sb.append(", oqsmajor=").append(oqsmajor);
        sb.append(", attribute='").append(attribute).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long id;
        private String entityClassF;
        private String attributeF;
        private String searchAttributeF;
        private long tx;
        private long commitId;
        private long createTime;
        private long updateTime;
        private long maintainId;
        private int oqsmajor;
        private String attribute;

        private Builder() {
        }

        public static Builder anManticoreStorageEntity() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withEntityClassF(String entityClassF) {
            this.entityClassF = entityClassF;
            return this;
        }

        public Builder withAttributeF(String attributeF) {
            this.attributeF = attributeF;
            return this;
        }

        public Builder withTx(long tx) {
            this.tx = tx;
            return this;
        }

        public Builder withCommitId(long commitId) {
            this.commitId = commitId;
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

        public Builder withMaintainId(long maintainId) {
            this.maintainId = maintainId;
            return this;
        }

        public Builder withOqsmajor(int oqsmajor) {
            this.oqsmajor = oqsmajor;
            return this;
        }

        public Builder withAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withSearchAttributeF(String searchAttributeF) {
            this.searchAttributeF = searchAttributeF;
            return this;
        }

        /**
         * 构造实例.
         *
         * @return 实例.
         */
        public SphinxQLStorageEntity build() {
            SphinxQLStorageEntity sphinxQLStorageEntity = new SphinxQLStorageEntity();
            sphinxQLStorageEntity.updateTime = this.updateTime;
            sphinxQLStorageEntity.id = this.id;
            sphinxQLStorageEntity.attribute = this.attribute;
            sphinxQLStorageEntity.tx = this.tx;
            sphinxQLStorageEntity.maintainId = this.maintainId;
            sphinxQLStorageEntity.createTime = this.createTime;
            sphinxQLStorageEntity.entityClassF = this.entityClassF;
            sphinxQLStorageEntity.commitId = this.commitId;
            sphinxQLStorageEntity.oqsmajor = this.oqsmajor;
            sphinxQLStorageEntity.attributeF = this.attributeF;
            sphinxQLStorageEntity.searchAttributeF = this.searchAttributeF;
            return sphinxQLStorageEntity;
        }
    }
}
