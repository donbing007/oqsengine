package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.task.UndoLogTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
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

    private UndoLogStore undoLogStore;

    private UndoLogTask logUndoTask;

    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;

    private Map<DbTypeEnum, Selector<DataSource>> dataSourceSelectors;

    private UndoExecutor undoExecutor;

    @PostConstruct
    public void init() {
        this.undoLogQ = new LinkedBlockingQueue<>();
        List<UndoLog> undoInfos = undoLogStore.loadAllUndoInfo();
        this.undoLogQ.addAll(undoInfos);

        UndoExecutor undoExecutor = new UndoExecutor(
                undoLogQ, undoLogStore, storageCommandInvokers);
        this.undoExecutor = undoExecutor;

        this.logUndoTask = new UndoLogTask(
                undoLogQ,
                undoLogStore,
                storageCommandInvokers,
                dataSourceSelectors);
        logUndoTask.start();
    }

    public UndoFactory(UndoLogStore undoLogStore){
        this.undoLogStore = undoLogStore;
        this.storageCommandInvokers = new HashMap<>();
        this.dataSourceSelectors = new HashMap<>();
    }

    public void register(DbTypeEnum dbType, StorageCommandInvoker cmdInvoker){
        if(dbType == null || cmdInvoker == null) {
            logger.error("Register failed. The dbType or invoker was null.");
            return;
        }

        storageCommandInvokers.put(dbType, cmdInvoker);
    }

    public void register(DbTypeEnum dbType, Selector<DataSource> selector){
        if(dbType == null || selector == null) {
            logger.error("Register failed. The dbType or selector was null.");
            return;
        }

        dataSourceSelectors.put(dbType, selector);
    }

    public UndoExecutor getUndoExecutor() {
        return this.undoExecutor;
    }

    @PreDestroy
    void destroy(){
        logger.debug("undo log task is stopped");
        logUndoTask.close();
    }
}
