package com.xforceplus.ultraman.oqsengine.storage.undo.task;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.UndoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/30/2020 5:26 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoLogTask extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(UndoLogTask.class);

    private volatile boolean closed;

    private BlockingQueue<UndoLog> undoLogQ;

    private UndoLogStore undoLogStore;

    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;

    private Map<DbTypeEnum, Selector<DataSource>> dataSourceSelectors;

    public UndoLogTask(BlockingQueue<UndoLog> undoLogQ,
                       UndoLogStore undoLogStore,
                       Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers,
                       Map<DbTypeEnum, Selector<DataSource>> dataSourceSelectors
    ) {
        this.closed = false;
        this.undoLogQ = undoLogQ;
        this.undoLogStore = undoLogStore;
        this.storageCommandInvokers = storageCommandInvokers;
        this.dataSourceSelectors = dataSourceSelectors;
    }

    @Override
    public void run() {
        loopHandleUndoLog();
        handleRemainingUndoLog();
    }

    void loopHandleUndoLog() {
        while(!closed) {
            try {
                handle(undoLogQ.take());
            } catch (InterruptedException e) {
                logger.info("The batch handler has been interrupted");
            }
        }
    }

    void handleRemainingUndoLog() {
        List<UndoLog> undoInfos = new ArrayList<>();
        undoLogQ.drainTo(undoInfos);
        for(UndoLog undoInfo:undoInfos) {
            handle(undoInfo);
        }
    }

    void handle(UndoLog undoInfo) {
        DataSource dataSource;

        if(undoInfo.getDbType() == null ||
                !this.dataSourceSelectors.containsKey(undoInfo.getDbType())) {
            String dbType = undoInfo.getDbType() == null ? null:undoInfo.getDbType().name();
            logger.error("can't find datasource select by dbType-{}", dbType);
            undoLogQ.add(undoInfo);
            return ;
        }

        dataSource = this.dataSourceSelectors
                .get(undoInfo.getDbType())
                .select(undoInfo.getShardKey());

        if(dataSource == null) {
            logger.error("can't find datasource by dbKey-{}", undoInfo.getShardKey());
            undoLogQ.add(undoInfo);
            return ;
        }

        StorageCommand cmd = UndoUtil.selectUndoStorageCommand(
                storageCommandInvokers, undoInfo.getDbType(), undoInfo.getOpType());

        if(cmd == null) {
            logger.error("can't find storage command by dbType-{}, opType-{}", undoInfo.getDbType(), undoInfo.getOpType());
            undoLogQ.add(undoInfo);
            return ;
        }

        TransactionResource resource;
        try {
            resource = buildResource(undoInfo.getDbType(), undoInfo.getShardKey(), dataSource.getConnection(), true);
        } catch (Exception e) {
            undoLogQ.add(undoInfo);
            return;
        }

        try {
            cmd.execute(resource, undoInfo.getData());
        } catch (SQLException e) {
            e.printStackTrace();
            undoLogQ.add(undoInfo);
            return;
        }

        undoLogStore.remove(undoInfo.getTxId(), undoInfo.getDbType(), undoInfo.getOpType());
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        this.closed = true;
        interrupt();
    }

    private TransactionResource buildResource(DbTypeEnum dbType, String key, Connection value, boolean autocommit)
            throws Exception {

        Class resourceClass = DbTypeEnum.INDEX.equals(dbType) ? SphinxQLTransactionResource.class :
                (DbTypeEnum.MASTOR.equals(dbType) ? ConnectionTransactionResource.class:null);

        Constructor<TransactionResource> constructor =
                resourceClass.getConstructor(String.class, Connection.class, Boolean.TYPE);
        return constructor.newInstance(key, value, autocommit);
    }
}
