package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/1/2020 3:43 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoLog {
    String dbType;
    String opType;
    Object data;

    public UndoLog(String dbType, String opType, Object data) {
        this.dbType = dbType;
        this.opType = opType;
        this.data = data;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
