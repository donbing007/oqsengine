package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;

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
public class RawEntry {
    private long id;
    private long executeTime;
    private CanalEntry.EventType eventType;
    private List<CanalEntry.Column> columns;

    public RawEntry(long id, long executeTime, CanalEntry.EventType eventType, List<CanalEntry.Column> columns) {
        this.id = id;
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

    public long getId() {
        return id;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawEntry rawEntry = (RawEntry) o;
        return id == rawEntry.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
