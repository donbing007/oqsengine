package com.xforceplus.ultraman.oqsengine.cdc.consumer.process;

import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import java.sql.SQLException;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public interface BatchProcessor {

    /**
     * 初始化.
     */
    void init();

    /**
     * 关闭.
     */
    void shutdown();

    /**
     * 错误.
     *
     * @param context 上下文.
     */
    void error(RunnerContext context);

    /**
     * 批次处理器.
     *
     * @param connector 连接器.
     * @param context   上下文.
     */
    void executeOneBatch(AbstractCDCConnector connector, RunnerContext context) throws SQLException;

    /**
     * 备份.
     *
     * @param connector 连接器.
     * @param context   上下文.
     */
    void recover(AbstractCDCConnector connector, RunnerContext context) throws SQLException;
}
