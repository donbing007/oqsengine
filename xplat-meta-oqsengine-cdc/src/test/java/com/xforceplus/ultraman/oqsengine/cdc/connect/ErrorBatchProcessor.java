package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.process.DefaultBatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ErrorBatchProcessor extends DefaultBatchProcessor {

    private int recovers = 0;

    /**
     * 模拟消费失败导致连接关闭.
     *
     * @param connector 连接器.
     * @param context 上下文.
     * @throws SQLException
     */
    @Override
    public void executeOneBatch(AbstractCDCConnector connector, RunnerContext context) throws SQLException {
        Assertions.assertNotNull(connector.getMessageWithoutAck());
        //  消费完抛出异常，触发重新连接.
        throw new SQLException("mock executeOneBatch, throw sqlException...");
    }

    /**
     * 恢复.
     * @param connector 连接器.
     * @param runnerContext 上下文.
     */
    @Override
    public void recover(AbstractCDCConnector connector, RunnerContext runnerContext) {
        //  已经开启了连接，需要设置为false.
        ((ErrorCDCConnector) connector).setShouldOpen(false);
        recovers++;
    }

    /**
     * 获得recover.
     *
     * @return recover次数.
     */
    public int getRecovers() {
        return recovers;
    }

}
