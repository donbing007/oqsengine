package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.TxUndoLog;

import java.util.List;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 1:59 PM
 * 功能描述:
 * 修改历史:
 */
public interface UndoLogStore {

    List<TxUndoLog> get(Long txId);

    void update(Long txId, List<TxUndoLog> txUndoLogs);

    void add(Long txId, TxUndoLog txUndoLog);

    void remove(Long txId);
}
