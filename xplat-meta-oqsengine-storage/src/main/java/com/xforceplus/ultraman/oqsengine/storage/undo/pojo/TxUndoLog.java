package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/23/2020 11:55 AM
 * 功能描述:
 * 修改历史:
 */

public class TxUndoLog {

    Long txId;

    String dbType;

    String opType;

    IEntity entity;

    public TxUndoLog(Long txId, String dbType, String opType, IEntity entity){
        this.txId = txId;
        this.dbType = dbType;
        this.opType = opType;
        this.entity = entity;
    }

    public Long getTxId() {
        return txId;
    }

    public String getDbType() {
        return dbType;
    }

    public String getOpType() {
        return opType;
    }

    public IEntity getEntity() {
        return entity;
    }
}
