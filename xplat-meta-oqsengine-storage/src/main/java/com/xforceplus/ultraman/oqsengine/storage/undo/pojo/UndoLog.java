package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/1/2020 3:43 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoLog {
    String dbKey;
    DbTypeEnum dbType;
    OpTypeEnum opType;
    Object data;

    public UndoLog(String dbKey, DbTypeEnum dbType, OpTypeEnum opType, Object data) {
        this.dbType = dbType;
        this.opType = opType;
        this.data = data;
        this.dbKey = dbKey;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
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
