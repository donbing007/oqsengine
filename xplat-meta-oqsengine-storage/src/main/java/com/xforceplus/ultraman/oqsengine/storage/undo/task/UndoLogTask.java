package com.xforceplus.ultraman.oqsengine.storage.undo.task;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建时间: 3/30/2020 5:26 PM
 * 功能描述:
 * 修改历史:
 * @author youyifan
 */
public class UndoLogTask extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(UndoLogTask.class);

    private volatile boolean closed;

    private Queue<UndoLog> undoLogQ;

    private UndoLogStore undoLogStore;

    private UndoExecutor undoExecutor;

    private Map<DbType, Selector<DataSource>> dataSourceSelectors;

    public UndoLogTask(
            UndoExecutor undoExecutor,
            UndoLogStore undoLogStore,
            Selector<DataSource> indexWriteDataSourceSelector,
            Selector<DataSource> masterDataSourceSelector
    ) {
        this.closed = false;
        this.undoLogStore = undoLogStore;
        this.undoLogQ = undoLogStore.getUndoLogQueue(
                Arrays.asList(
                        UndoLogStatus.UNCOMMITTED.value(),
                        UndoLogStatus.COMMITED.value(),
                        UndoLogStatus.ERROR.value())
        );

        this.undoExecutor = undoExecutor;
        this.dataSourceSelectors = new HashMap();
        this.dataSourceSelectors.put(DbType.INDEX, indexWriteDataSourceSelector);
        this.dataSourceSelectors.put(DbType.MASTER, masterDataSourceSelector);

    }

    @Override
    public void run() {
        loopHandleUndoLog();
    }

    void loopHandleUndoLog() {
        while (!closed) {
            if (undoLogQ.isEmpty()) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    logger.error("Thread sleep interrupted");
                }
                undoLogQ = undoLogStore.getUndoLogQueue(
                        Arrays.asList(
                                UndoLogStatus.ERROR.value()
                        ));
            }
            handle(undoLogQ.poll());
        }
    }

    void handle(UndoLog undoLog) {
        if (undoLog == null) {
            return;
        }

        if (undoLog.getStatus() == UndoLogStatus.UNCOMMITTED.value()) {
            undoLogStore.remove(undoLog.getTxId(), undoLog.getDbType(), undoLog.getShardKey());
        } else if (undoLog.getStatus() == UndoLogStatus.COMMITED.value()) {
            undoLogStore.updateStatus(undoLog.getTxId(), undoLog.getDbType(), undoLog.getShardKey(), UndoLogStatus.ERROR);
        }

        DataSource dataSource;

        if (undoLog.getDbType() == null ||
                !this.dataSourceSelectors.containsKey(undoLog.getDbType())) {
            String dbType = undoLog.getDbType() == null ? null : undoLog.getDbType().name();
            logger.error("can't find datasource select by dbType-{}", dbType);
            return;
        }

        dataSource = this.dataSourceSelectors
                .get(undoLog.getDbType())
                .select(undoLog.getShardKey());

        if (dataSource == null) {
            logger.error("can't find datasource by dbKey-{}", undoLog.getShardKey());
            return;
        }

        TransactionResource resource;
        try {
            resource = buildResource(undoLog.getDbType(), undoLog.getShardKey(), dataSource.getConnection(), true);
        } catch (Exception e) {
            logger.error("failed to build resource dbtype-{}, shardKey-{} ", undoLog.getDbType(), undoLog.getShardKey());
            return;
        }

        ((UndoTransactionResource) resource).setUndoLog(undoLog);

        undoExecutor.undo(resource);
    }

    public void close() {
        this.closed = true;
        interrupt();
    }

    private TransactionResource buildResource(DbType dbType, String key, Connection value, boolean autocommit)
            throws Exception {

        Class resourceClass = DbType.INDEX.equals(dbType) ? SphinxQLTransactionResource.class :
                (DbType.MASTER.equals(dbType) ? ConnectionTransactionResource.class : null);

        Constructor<TransactionResource> constructor =
                resourceClass.getConstructor(String.class, Connection.class, Boolean.TYPE);
        return constructor.newInstance(key, value, autocommit);
    }
}
