package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.SQLException;

/**
 * 表示一个任务.
 *
 * @param <R> 返回结果.
 * @author dongbin
 * @version 0.1 2020/2/17 15:22
 * @since 1.8
 */
public interface ResourceTask<R> {

    /**
     * 执行任务.
     *
     * @param transaction 相关事务.
     * @param resource    相关资源.
     * @return 执行结果.
     * @throws SQLException 执行异常.
     */
    R run(Transaction transaction, TransactionResource resource) throws Exception;

    /**
     * 判断是否依附在 master 资源上.
     * 所谓的依赖即表示会使用 master 的事务资源相关,本身不再处理事务提交和回滚.
     * 除非没有可依附的.
     *
     * @return true依附, false不依附.
     */
    default boolean isAttachmentMaster() {
        return false;
    }

    /**
     * 任务key.
     *
     * @return 任务key.
     */
    default String key() {
        return "";
    }

}
