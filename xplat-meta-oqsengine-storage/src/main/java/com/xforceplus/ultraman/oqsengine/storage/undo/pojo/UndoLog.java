package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import java.io.Serializable;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/1/2020 5:46 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoLog implements Serializable {
    Long txId;
    String shardKey;
    DbTypeEnum dbType;
    OpTypeEnum opType;
    Object data;

    public UndoLog(Long txId, String dbKey, DbTypeEnum dbType, OpTypeEnum opType, Object data) {
        this.txId = txId;
        this.shardKey = dbKey;
        this.dbType = dbType;
        this.opType = opType;
        this.data = data;
    }

    public Long getTxId() {
        return txId;
    }

    public void setTxId(Long txId) {
        this.txId = txId;
    }

    public String getShardKey() {
        return shardKey;
    }

    public void setShardKey(String shardKey) {
        this.shardKey = shardKey;
    }

    public DbTypeEnum getDbType() {
        return dbType;
    }

    public void setDbType(DbTypeEnum dbType) {
        this.dbType = dbType;
    }

    public OpTypeEnum getOpType() {
        return opType;
    }

    public void setOpType(OpTypeEnum opType) {
        this.opType = opType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
