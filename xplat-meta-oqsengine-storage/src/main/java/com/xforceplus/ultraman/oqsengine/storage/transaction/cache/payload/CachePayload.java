package com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

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
    private long txId;
    private long number;
    private long entityId;
    private long version;
    private Map<IEntityField, Object> entityValues;

    public CachePayload() {
    }

    public CachePayload(long txId, long number, long version, long entityId, Map<IEntityField, Object> entityValues) {
        this.txId = txId;
        this.number = number;
        this.entityId = entityId;
        this.version = version;
        this.entityValues = entityValues;
    }

    public long getTxId() {
        return txId;
    }

    public long getNumber() {
        return number;
    }

    public Map<IEntityField, Object>  getEntityValues() {
        return entityValues;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public void setEntityValues(Map<IEntityField, Object> entityValues) {
        this.entityValues = entityValues;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
