package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/8/2020 11:00 AM
 * 功能描述:
 * 修改历史:
 */
public abstract class AbstractTransactionResource<V> implements TransactionResource<V> {

    protected List<UndoInfo> undoInfos;

    public AbstractTransactionResource(){
        undoInfos = new ArrayList<>();
    }

    public void addUndoInfo(String dbKey, OpTypeEnum opType, Object obj) {
        this.undoInfos.add(new UndoInfo(null, dbKey, dbType(), opType, obj));
    }

    public List<UndoInfo> undoInfos() {
        return undoInfos;
    }
}
