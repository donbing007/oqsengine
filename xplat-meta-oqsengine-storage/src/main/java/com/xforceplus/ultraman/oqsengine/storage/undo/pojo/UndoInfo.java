package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/1/2020 5:46 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoInfo {
    Long txId;
    DbTypeEnum dbType;
    OpTypeEnum opType;
    Object data;

    public UndoInfo(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object data) {
        this.txId = txId;
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
