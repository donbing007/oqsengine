package com.xforceplus.ultraman.oqsengine.storage.undo.thread;

import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/30/2020 5:26 PM
 * 功能描述:
 * 修改历史:
 */
public class TransactionResourceHandler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResourceHandler.class);

    private BlockingQueue<UndoLog> undoLogQ;

    private volatile boolean closed;

    public TransactionResourceHandler(BlockingQueue<UndoLog> undoLogQ) {
        this.undoLogQ = undoLogQ;
        this.closed = false;
    }

    @Override
    public void run(){
        loopHandleTransactionResource();
        handleRemainingTransactionResource();
    }

    void loopHandleTransactionResource(){
        while(!closed) {
            try {
                handle(undoLogQ.take());
            } catch (InterruptedException e) {
                LOGGER.info("The batch handler has been interrupted");
            }
        }
    }

    void handleRemainingTransactionResource() {
        List<UndoLog> undoLogs = new ArrayList<>();
        undoLogQ.drainTo(undoLogs);
        for(UndoLog undoLog:undoLogs) {
            handle(undoLog);
        }
    }

    void handle(UndoLog undoLog) {
//        try {
//            undoLog.destroy();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        this.closed = true;
        interrupt();
    }
}
