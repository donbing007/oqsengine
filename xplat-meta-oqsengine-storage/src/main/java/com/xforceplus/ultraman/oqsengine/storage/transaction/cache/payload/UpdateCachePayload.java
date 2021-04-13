package com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.io.Serializable;
import java.util.Map;

/**
 * desc :
 * name : UpdateCachePayload
 *
 * @author : xujia
 * date : 2021/4/12
 * @since : 1.8
 */
public class UpdateCachePayload extends CachePayload implements Serializable {
    private Map<IEntityField, Object> oldEntityValues;

    public UpdateCachePayload() {
    }

    public UpdateCachePayload(long txId, long number, long version, long entityId,
                              Map<IEntityField, Object> entityValues, Map<IEntityField, Object> oldEntityValues) {
        super(txId, number, version, entityId, entityValues);
        this.oldEntityValues = oldEntityValues;
    }

    public Map<IEntityField, Object> getOldEntityValues() {
        return oldEntityValues;
    }

    public void setOldEntityValues(Map<IEntityField, Object> oldEntityValues) {
        this.oldEntityValues = oldEntityValues;
    }
}
