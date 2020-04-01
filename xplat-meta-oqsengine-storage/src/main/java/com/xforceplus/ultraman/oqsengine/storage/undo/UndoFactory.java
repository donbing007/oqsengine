package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.thread.TransactionResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 5:52 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoFactory {

    final Logger logger = LoggerFactory.getLogger(UndoFactory.class);

    private LinkedBlockingQueue<UndoLog> undoLogQ;

    private TransactionResourceHandler transactionResourceHandler;

    private UndoLogStore undoLogStore;

    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;

    @PostConstruct
    public void init() {
        this.storageCommandInvokers = new HashMap<>();
        this.undoLogQ = new LinkedBlockingQueue<>();

        this.transactionResourceHandler =
                new TransactionResourceHandler(undoLogQ);

        transactionResourceHandler.start();
    }

    public UndoFactory(UndoLogStore undoLogStore){
        this.undoLogStore = undoLogStore;
    }

    public void register(DbTypeEnum dbType, StorageCommandInvoker cmdInvoker){
        if(dbType == null || cmdInvoker == null) {
            logger.error("Register failed. The dbType or invoker was null.");
            return;
        }

        storageCommandInvokers.put(dbType, cmdInvoker);
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

    private void saveLog(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object object){
        undoLogStore.save(txId, dbType, opType, object);
    }

    public UndoExecutor getUndo(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object undoData) {
//        saveLog(txId, dbType, opType, undoData);
        OpTypeEnum undoOpType = null;
        switch (opType) {
            case BUILD: undoOpType = OpTypeEnum.DELETE; break;
            case DELETE: undoOpType = OpTypeEnum.BUILD; break;
            case REPLACE: undoOpType = OpTypeEnum.REPLACE; break;
            case REPLACE_ATTRIBUTE: undoOpType = OpTypeEnum.REPLACE_ATTRIBUTE; break;
            default:
        }

        if(undoOpType == null) {
            logger.error("Can't find undo OpType by {}", opType);
            return null;
        }

        UndoExecutor undoExecutor = new UndoExecutor(getStorageCommand(dbType, undoOpType), undoData);
        undoExecutor.setErrorTransactionResourceQ(undoLogQ);

        return undoExecutor;
    }

    @PreDestroy
    void destroy(){
        transactionResourceHandler.close();
    }
}
