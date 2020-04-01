package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum.REPLACE_ATTRIBUTE;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 4:38 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoExecutor {

    final Logger logger = LoggerFactory.getLogger(UndoExecutor.class);

    private BlockingQueue<UndoLog> undoLogQ;
    private UndoLogStore undoLogStore;
    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;

    public UndoExecutor(Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers) {
        this.storageCommandInvokers = storageCommandInvokers;
    }

    public void undo(TransactionResource resource) {
        UndoInfo undoInfo = resource.getUndoInfo();

        OpTypeEnum undoOpType = null;
        switch (undoInfo.getOpType()) {
            case BUILD: undoOpType = OpTypeEnum.DELETE; break;
            case DELETE: undoOpType = OpTypeEnum.BUILD; break;
            case REPLACE: undoOpType = OpTypeEnum.REPLACE; break;
            case REPLACE_ATTRIBUTE: undoOpType = REPLACE_ATTRIBUTE; break;
            default:
        }

        if(undoOpType == null) {
            logger.error("Can't find undo OpType by {}", undoInfo.getOpType());
        }

        StorageCommand cmd = getStorageCommand(undoInfo.getDbType(), undoInfo.getOpType());

        try {
            cmd.execute(resource, undoInfo.getData());
            removeUndoLog(resource);
        } catch (SQLException e) {
            if(undoLogQ != null) {
                undoLogQ.add(new UndoLog(undoInfo.getDbType(), undoInfo.getOpType(), undoInfo.getData()));
            }
        }
    }

    public void setUndoQ(BlockingQueue<UndoLog> undoLogQ) {
        this.undoLogQ = undoLogQ;
    }

    public void saveUndoLog(TransactionResource resource){
        UndoInfo undoInfo = resource.getUndoInfo();

        if(this.undoLogStore != null) {
            this.undoLogStore.save(undoInfo.getTxId(), undoInfo.getDbType(),
                    undoInfo.getOpType(), undoInfo.getData());
        }
    }

    public void removeUndoLog(TransactionResource resource){
        UndoInfo undoInfo = resource.getUndoInfo();
        if(this.undoLogStore != null) {
            this.undoLogStore.remove(undoInfo.getTxId(), undoInfo.getDbType(), undoInfo.getOpType());
        }
    }

    public void removeTxUndoLog(Long txId){
        if(this.undoLogStore != null) {
            this.undoLogStore.remove(txId);
        }
    }

    public void setUndoLogStore(UndoLogStore undoLogStore) {
        this.undoLogStore = undoLogStore;
    }

    public StorageCommand getStorageCommand(DbTypeEnum dbType, OpTypeEnum opType){
        if(dbType == null || opType == null) {
            logger.error("GetStorageCommand failed. The dbType or opType was null.");
            return null; }

        if(!storageCommandInvokers.containsKey(dbType) || storageCommandInvokers.get(dbType) == null) {
            logger.error("Can't find storageCommand which dbType is {}", dbType.name());
            return null;
        }

        return storageCommandInvokers.get(dbType).selectCommand(opType);
    }

}
