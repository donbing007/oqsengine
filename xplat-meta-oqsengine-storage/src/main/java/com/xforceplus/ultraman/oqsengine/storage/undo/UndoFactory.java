package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.task.UndoLogTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 5:52 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoFactory {

    final Logger logger = LoggerFactory.getLogger(UndoFactory.class);

    @Resource
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private Selector<DataSource> masterDataSourceSelector;

    @Resource
    private StorageCommandExecutor storageCommandInvoker;

    @Resource
    private UndoLogStore undoLogStore;

    private UndoLogTask logUndoTask;

    private UndoExecutor undoExecutor;

    @PostConstruct
    public void init() {
        this.undoExecutor = new UndoExecutor(undoLogStore, storageCommandInvoker);

        this.logUndoTask = new UndoLogTask(
                undoExecutor,
                undoLogStore,
                indexWriteDataSourceSelector,
                masterDataSourceSelector);

        logUndoTask.start();
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
