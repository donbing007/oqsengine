package com.xforceplus.ultraman.oqsengine.storage.undo.thread;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.UndoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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
public class LogUndoHandler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LogUndoHandler.class);

    private volatile boolean closed;

    private BlockingQueue<UndoInfo> undoLogQ;

    private UndoLogStore undoLogStore;

    private Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers;

    private Map<DbTypeEnum, Selector<DataSource>> dataSourceSelectors;

    public LogUndoHandler(BlockingQueue<UndoInfo> undoLogQ,
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
    public void run(){
        loopHandleUndoLog();
        handleRemainingUndoLog();
    }

    void loopHandleUndoLog(){
        while(!closed) {
            try {
                handle(undoLogQ.take());
            } catch (InterruptedException e) {
                logger.info("The batch handler has been interrupted");
            }
        }
    }

    void handleRemainingUndoLog() {
        List<UndoInfo> undoInfos = new ArrayList<>();
        undoLogQ.drainTo(undoInfos);
        for(UndoInfo undoInfo:undoInfos) {
            handle(undoInfo);
        }
    }

    void handle(UndoInfo undoInfo) {
        DataSource dataSource;

        if(undoInfo.getDbType() == null ||
                !this.dataSourceSelectors.containsKey(undoInfo.getDbType())) {
            String dbType = undoInfo.getDbType() == null ? null:undoInfo.getDbType().name();
            logger.error("can't find datasource select by dbType-{}", dbType);
            return ;
        }

        dataSource = this.dataSourceSelectors
                .get(undoInfo.getDbType())
                .select(undoInfo.getShardKey());

        if(dataSource == null) {
            logger.error("can't find datasource by dbKey-{}", undoInfo.getShardKey());
            return ;
        }

        StorageCommand cmd = UndoUtil.selectUndoStorageCommand(
                storageCommandInvokers, undoInfo.getDbType(), undoInfo.getOpType());

        if(cmd == null) {
            logger.error("can't find storage command by dbType-{}, opType-{}",
                    undoInfo.getShardKey(), undoInfo.getOpType());
            return ;
        }

        try {
            cmd.execute(dataSource.getConnection(), undoInfo.getData());
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
}
