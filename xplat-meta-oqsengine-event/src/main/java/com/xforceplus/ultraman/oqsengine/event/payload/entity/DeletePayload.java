package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 删除的事务负载.
 * 记录了被删除的实体快照 .
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:44
 * @since 1.8
 */
public class DeletePayload extends BuildPayload {

    public DeletePayload(long txId, IEntity ...deletedEntities) {
        super(txId, deletedEntities);
    }

}
