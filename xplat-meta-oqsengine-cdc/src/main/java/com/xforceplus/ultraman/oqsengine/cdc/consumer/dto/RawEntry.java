package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * desc :
 * name : RawEntry
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class RawEntry implements Serializable {

    private long executeTime;
    private CanalEntry.EventType eventType;
    private List<CanalEntry.Column> columns;

    public RawEntry(long executeTime, CanalEntry.EventType eventType, List<CanalEntry.Column> columns) {
        this.executeTime = executeTime;
        this.eventType = eventType;
        this.columns = columns;
    }

    public CanalEntry.EventType getEventType() {
        return eventType;
    }

    public List<CanalEntry.Column> getColumns() {
        return columns;
    }


    public long getExecuteTime() {
        return executeTime;
    }
}
