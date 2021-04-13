package com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * desc :
 * name : CachePayload
 *
 * @author : xujia
 * date : 2021/4/12
 * @since : 1.8
 */
public class CachePayload implements Serializable {
    /**
     * txId
     */
    @JsonProperty(value = "txId")
    private long txId;
    /**
     * entity的主键ID
     */
    @JsonProperty(value = "id")
    private long id;
    /**
     * entity的当前版本
     */
    @JsonProperty(value = "version")
    private int version;

    /**
     * entity在当前TX中的操作顺序
     */
    @JsonProperty(value = "number")
    private long number;

    /**
     * entity中的entityFieldId-value(toString)键值对
     */
    @JsonProperty(value = "fieldValueMapping")
    private Map<Long, String> fieldValueMapping;

    /**
     * entity中的entityFieldId-value(toString)键值对
     */
    @JsonProperty(value = "oldFieldValueMapping")
    private Map<Long, String> oldFieldValueMapping;

    public long getTxId() {
        return txId;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public long getNumber() {
        return number;
    }

    public Map<Long, String> getFieldValueMapping() {
        return fieldValueMapping;
    }

    public Map<Long, String> getOldFieldValueMapping() {
        return oldFieldValueMapping;
    }

    /**
     * builder
     */
    public static class Builder {
        private long txId;
        private long id;
        private int version;
        private long number;
        private Map<Long, String> fieldValueMapping;
        private Map<Long, String> oldFieldValueMapping;

        private Builder() {
        }

        public static CachePayload.Builder anCacheValue() {
            return new CachePayload.Builder();
        }

        public CachePayload.Builder withTxId(long txId) {
            this.txId = txId;
            return this;
        }

        public CachePayload.Builder withId(long id) {
            this.id = id;
            return this;
        }

        public CachePayload.Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public CachePayload.Builder withNumber(long number) {
            this.number = number;
            return this;
        }

        public CachePayload.Builder withFieldValueMapping(Map<Long, String> fieldValueMapping) {
            this.fieldValueMapping = fieldValueMapping;
            return this;
        }

        public CachePayload.Builder withOldFieldValueMapping(Map<Long, String> oldFieldValueMapping) {
            this.oldFieldValueMapping = oldFieldValueMapping;
            return this;
        }

        public CachePayload build() {
            CachePayload value = new CachePayload();
            value.txId = this.txId;
            value.id = this.id;
            value.version = this.version;
            value.number = this.number;
            value.fieldValueMapping = this.fieldValueMapping;
            value.oldFieldValueMapping = this.oldFieldValueMapping;
            return value;
        }
    }
}
