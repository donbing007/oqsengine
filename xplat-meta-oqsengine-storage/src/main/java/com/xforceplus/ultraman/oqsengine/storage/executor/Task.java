package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;

import java.sql.SQLException;

/**
 * 表示一个任务.
 *
 * @param <V> 任务处理数据类型.
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
    Object run(V resource, ExecutorHint hint) throws SQLException;

}
