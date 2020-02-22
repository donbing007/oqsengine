package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 表示一个任务.
 * @author dongbin
 * @version 0.1 2020/2/17 15:22
 * @since 1.8
 */
public interface Task<V> {

    /**
     * 执行任务.
     * @param resource
     * @return
     * @throws SQLException
     */
    Object run(V resource) throws SQLException;
}
