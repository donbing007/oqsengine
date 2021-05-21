package com.xforceplus.ultraman.oqsengine.common.executor;

import java.sql.SQLException;

/**
 * 执行器定义.
 *
 * @param <R> 请求资源.
 * @param <T> 执行结果.
 * @author dongbin
 * @version 0.1 2020/11/2 14:37
 * @since 1.8
 */
public interface Executor<R, T> {

    /**
     * 执行.
     *
     * @param r 请求资源.
     * @return 操作结果.
     * @throws Exception 执行异常.
     */
    T execute(R r) throws SQLException;
}
