package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * @author dongbin
 * @version 0.1 2021/3/24 15:43
 * @since 1.8
 */
public class ReplacePayload extends BuildPayload {
    public ReplacePayload(long txId, long number, IEntity entity) {
        super(txId, number, entity);
    }
}
