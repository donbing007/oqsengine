package com.xforceplus.ultraman.oqsengine.storage.master.pojo;

import java.io.Serializable;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   leo
 * 创建时间: 4/17/2020 10:20 AM
 * 功能描述:
 * 修改历史:
 *
 * @author leo
 */
public class StorageUniqueEntity implements Serializable {

    private long id;
    private long[] entityClasses;
    private String key;


    public StorageUniqueEntity(long id, long[] entity, String key) {
        this.id = id;
        this.entityClasses = entity;
        this.key = key;
    }

    public static StorageUniqueEntity.StorageUniqueEntityBuilder builder() {
        return new StorageUniqueEntity.StorageUniqueEntityBuilder();
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long[] getEntityClasses() {
        return entityClasses;
    }

    public void setEntityClasses(long[] entityClasses) {
        this.entityClasses = entityClasses;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("StorageEntity{").append("id=").append(id).append(",entityClasses=");
        for (long classId : entityClasses) {
            buffer.append(classId).append(",");
        }
        buffer.append("key=").append(key).append("}");
        return buffer.toString();
    }

    /**
     *
     */
    public static class StorageUniqueEntityBuilder {
        private long id;
        private long[] entityClasses;
        private String key;

        public StorageUniqueEntityBuilder() {
        }

        public StorageUniqueEntity.StorageUniqueEntityBuilder id(final long id) {
            this.id = id;
            return this;
        }

        public StorageUniqueEntity.StorageUniqueEntityBuilder entityClasses(final long[] entity) {
            this.entityClasses = entity;
            return this;
        }

        public StorageUniqueEntity.StorageUniqueEntityBuilder key(final String key) {
            this.key = key;
            return this;
        }

        public StorageUniqueEntity build() {
            return new StorageUniqueEntity(this.id, this.entityClasses, this.key);
        }

    }
}
