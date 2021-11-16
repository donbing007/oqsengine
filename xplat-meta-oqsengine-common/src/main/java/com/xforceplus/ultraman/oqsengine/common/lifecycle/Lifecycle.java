package com.xforceplus.ultraman.oqsengine.common.lifecycle;

import java.sql.SQLException;

/**
 * 生命周期定义.
 *
 * @author dongbin
 * @version 0.1 2021/04/02 11:01
 * @since 1.8
 */
public interface Lifecycle {

    /**
     * 初始化.
     *
     * @throws SQLException 初始化失败.
     */
    default void init() throws Exception {

    }

    /**
     * 清理.
     *
     * @throws SQLException 清理失败.
     */
    default void destroy() throws Exception {

    }

}
