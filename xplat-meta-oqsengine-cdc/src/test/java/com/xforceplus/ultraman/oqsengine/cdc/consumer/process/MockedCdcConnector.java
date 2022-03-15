package com.xforceplus.ultraman.oqsengine.cdc.consumer.process;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class MockedCdcConnector extends AbstractCDCConnector {

    private long ackBatchId = -1;
    private long batchId = 0;
    private List<CanalEntry.Entry> entries;

    /**
     * 构造器.
     *
     */
    public MockedCdcConnector() {
        super("", "", "", "");
    }

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {

    }


    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public void rollback() {
        batchId = ackBatchId;
    }

    public void ack(long batchId) throws SQLException {
        ackBatchId = batchId;
    }

    /**
     * 不自动确认获取消息.
     *
     * @return 消息实例.
     * @throws SQLException 获取发生异常.
     */
    @Override
    public Message getMessageWithoutAck() throws SQLException {
        if (null == entries || entries.isEmpty()) {
            return new Message(batchId++, null);
        }
        return new Message(batchId++, entries);
    }

    public long getAckBatchId() {
        return ackBatchId;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setEntries(List<CanalEntry.Entry> entries) {
        this.entries = entries;
    }

}
