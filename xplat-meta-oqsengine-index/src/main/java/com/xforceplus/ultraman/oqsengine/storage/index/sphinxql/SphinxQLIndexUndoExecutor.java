package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.TxUndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 6:06 PM
 * 功能描述:
 * 修改历史:
 */
public class SphinxQLIndexUndoExecutor extends UndoExecutor {

    Connection conn;

    SphinxQLIndexAction action;

    UndoLogStore undoLogStore;

    Long txId;

    DbTypeEnum dbType;

    public SphinxQLIndexUndoExecutor(SphinxQLIndexAction action, UndoLogStore undoLogStore) {
        this.action = action;
        this.undoLogStore = undoLogStore;
    }

    @Override
    public void setTxId(Long txId) {
        this.txId = txId;
    }

    @Override
    public void setDbType(DbTypeEnum dbType) {
        this.dbType = dbType;
    }

    @Override
    public void build() throws SQLException {
        //获得undo数据
        action.build(conn, null);
    }

    @Override
    public void replace() throws SQLException {
        //获得undo数据
        action.replace(conn, null);
    }

    @Override
    public void delete() throws SQLException {
        //获得undo数据
        action.delete(conn, null);
    }

    @Override
    public void saveUndoLog(DbTypeEnum dbTypeEnum, OpTypeEnum opTypeEnum, Long txId, IEntity entity){
        undoLogStore.add(txId, new TxUndoLog(txId, dbTypeEnum.value(), opTypeEnum.value(), entity));
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
