package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;

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
    TransactionResourceType transactionResourceType;
    String shardKey;
    int status = 0;
    long time;
    List<UndoLogItem>  items = new ArrayList<>();

    public UndoLog() {

    }

    public UndoLog(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        this.txId = txId;
        this.shardKey = shardKey;
        this.transactionResourceType = transactionResourceType;
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

    public TransactionResourceType getTransactionResourceType() {
        return transactionResourceType;
    }

    public void setTransactionResourceType(TransactionResourceType transactionResourceType) {
        this.transactionResourceType = transactionResourceType;
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
