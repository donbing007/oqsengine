package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.AbstractTransactionResource;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/9/2020 2:00 PM
 * 功能描述:
 * 修改历史:
 */
public abstract class AbstractStorageCommand<T> implements StorageCommand<T>{
    public void recordOriginalData(TransactionResource resource, OpTypeEnum opType, T data){
        ((AbstractTransactionResource)resource).addUndoInfo(null, opType, data);
    }
}
