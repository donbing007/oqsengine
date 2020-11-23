package com.xforceplus.ultraman.oqsengine.devops.executor;

import java.sql.SQLException;

/**
 * desc :
 * name : DevOpsExecutor
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public interface DevOpsExecutor<T, R> {

    /**
     * 执行.
     *
     * @param res 请求资源.
     * @return 操作结果.
     * @throws Exception 执行异常.
     */
    R execute(T res) throws SQLException;
}