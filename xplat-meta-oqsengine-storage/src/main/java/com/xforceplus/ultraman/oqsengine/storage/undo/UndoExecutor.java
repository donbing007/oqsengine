package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.AbstractTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.UndoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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

    private BlockingQueue<UndoInfo> undoLogQ;
    private UndoLogStore undoLogStore;
    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;
    private boolean mockError;

    public UndoExecutor(
            BlockingQueue<UndoInfo> undoLogQ,
            UndoLogStore undoLogStore,
            Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers) {
        this.undoLogQ = undoLogQ;
        this.undoLogStore = undoLogStore;
        if(undoLogStore == null) {
            logger.debug("UndoExecutor set UndoLogStore to null");
        }
        this.storageCommandInvokers = storageCommandInvokers;
        this.mockError = false;
    }

    public void undo(TransactionResource res) {
        AbstractTransactionResource resource = (AbstractTransactionResource) res;

        List<UndoInfo> undoInfos = resource.undoInfos();

        logger.debug("[UndoExecutor UNDO] start undo {} items", undoInfos.size());
        for(UndoInfo undoInfo:undoInfos) {
            OpTypeEnum undoOpType = null;
            switch (undoInfo.getOpType()) {
                case BUILD:
                    undoOpType = OpTypeEnum.DELETE;
                    break;
                case DELETE:
                    undoOpType = OpTypeEnum.BUILD;
                    break;
                case REPLACE:
                    undoOpType = OpTypeEnum.REPLACE;
                    break;
                case REPLACE_ATTRIBUTE:
                    undoOpType = REPLACE_ATTRIBUTE;
                    break;
                default:
            }

            if (undoOpType == null) {
                String errMsg = String.format("Can't find undo OpType by %s", undoInfo.getOpType().name());
                logger.error(errMsg);
                throw new RuntimeException(errMsg);
            }

            StorageCommand undoCmd = UndoUtil.selectUndoStorageCommand(
                    storageCommandInvokers, undoInfo.getDbType(), undoInfo.getOpType());

            if (undoCmd == null) {
                String errMsg = String.format("Can't find undo Storage Command by dbType-{} opType-{}", undoInfo.getDbType().name(), undoInfo.getOpType().name());
                logger.error(errMsg);
                throw new RuntimeException(errMsg);
            }

            logger.debug("[UndoExecutor UNDO] undo operate {} ", undoOpType.name());
            try {
                undoCmd.execute((Connection) resource.value(), undoInfo.getData());

                removeUndoLog(undoInfo);
            } catch (Exception e) {
                logger.debug("[UndoExecutor UNDO] undo failed, add into Queue ", undoOpType.name());
                if (undoLogQ != null) {
                    undoLogQ.add(undoInfo);
                }

                e.printStackTrace();
            }
        }

        logger.debug("[UndoExecutor UNDO] finish undo {} items", undoInfos.size());
    }

    public void saveUndoLog(Long txId, TransactionResource res){
        AbstractTransactionResource resource = (AbstractTransactionResource) res;
        if (this.undoLogStore != null) {
            List<UndoInfo> undoInfos = resource.undoInfos();
            logger.debug("[UndoExecutor UNDO] save undo infos {} items in store ", undoInfos.size());
            for(UndoInfo undoInfo:undoInfos) {
                this.undoLogStore.save(txId, undoInfo.getShardKey(),
                        undoInfo.getDbType(), undoInfo.getOpType(), undoInfo.getData());
            }
        }
    }

    public void removeUndoLog(UndoInfo undoInfo){
        if (this.undoLogStore != null) {
            this.undoLogStore.remove(undoInfo.getTxId(), undoInfo.getDbType(), undoInfo.getOpType());
            logger.debug("[UndoExecutor UNDO] success to clear undo log in store");
        }
    }

    public void removeTxUndoLog(Long txId){
        if(this.undoLogStore != null) {
            if(!this.undoLogStore.isExist(txId)) {
                logger.debug("[removeTxUndoLog] tx {} has no undo log", txId);
            }
            this.undoLogStore.remove(txId);
        }
    }

    public void mock() throws SQLException {
        if(mockError) {
            throw new SQLException("mock throws SQLException when commits finished");
        }
    }

    public void setMockError(boolean mockError) {
        this.mockError = mockError;
    }
}
