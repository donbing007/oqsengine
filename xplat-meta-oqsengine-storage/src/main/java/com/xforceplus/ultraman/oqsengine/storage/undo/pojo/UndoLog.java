package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/1/2020 5:46 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoLog implements Serializable {
    Long txId;
    DbType dbType;
    String shardKey;
    int status = 0;
    long time;
    List<UndoLogItem>  items = new ArrayList<>();

    public UndoLog() {

    }

    public UndoLog(Long txId, DbType dbType, String shardKey) {
        this.txId = txId;
        this.shardKey = shardKey;
        this.dbType = dbType;
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

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<UndoLogItem> getItems() {
        return items;
    }

    public void setItems(List<UndoLogItem> items) {
        this.items = items;
    }

}
