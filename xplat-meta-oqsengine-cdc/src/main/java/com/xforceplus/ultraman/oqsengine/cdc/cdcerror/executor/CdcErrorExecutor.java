package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor;

import java.sql.SQLException;

/**
 * cdc 错误信息执行处理器.
 *
 * @param <T> 请求.
 * @param <R> 响应.
 * @author xujia 2020/11/21
 * @since : 1.8
 */
public interface CdcErrorExecutor<T, R> {

    /**
     * 执行.
     *
     * @param res 请求资源.
     * @return 操作结果.
     * @throws Exception 执行异常.
     */
    R execute(T res) throws SQLException;
}
