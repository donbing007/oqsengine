package com.xforceplus.ultraman.oqsengine.common.executor;

import java.sql.SQLException;

/**
 * 执行器定义.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 14:37
 * @since 1.8
 */
public interface Executor<RES, REQ> {

    /**
     * 执行.
     *
     * @param res
     * @return
     * @throws Exception
     */
    REQ execute(RES res) throws SQLException;
}
