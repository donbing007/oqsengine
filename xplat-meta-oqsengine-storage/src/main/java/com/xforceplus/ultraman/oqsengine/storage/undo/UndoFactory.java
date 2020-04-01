package com.xforceplus.ultraman.oqsengine.storage.undo;

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
import java.util.concurrent.LinkedBlockingQueue;

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

    private UndoExecutor undoExecutor;

    @PostConstruct
    public void init() {
        this.storageCommandInvokers = new HashMap<>();
        this.undoLogQ = new LinkedBlockingQueue<>();

        this.transactionResourceHandler =
                new TransactionResourceHandler(undoLogQ);

        transactionResourceHandler.start();

        UndoExecutor undoExecutor = new UndoExecutor(storageCommandInvokers);
        undoExecutor.setUndoLogStore(undoLogStore);
        undoExecutor.setUndoQ(undoLogQ);
        this.undoExecutor = undoExecutor;
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

    private void saveLog(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object object){
        undoLogStore.save(txId, dbType, opType, object);
    }

    public UndoExecutor getUndoExecutor() {
        return undoExecutor;
    }

    @PreDestroy
    void destroy(){
        transactionResourceHandler.close();
    }
}
