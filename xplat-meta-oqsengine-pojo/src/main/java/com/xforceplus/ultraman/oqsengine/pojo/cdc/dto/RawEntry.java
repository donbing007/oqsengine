package com.xforceplus.ultraman.oqsengine.pojo.cdc.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.io.Serializable;
import java.util.List;

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
    private long id;
    private long commitId;
    private List<CanalEntry.Column> columns;

    public RawEntry(long id, long commitId, long executeTime, List<CanalEntry.Column> columns) {
        this.id = id;
        this.commitId = commitId;
        this.executeTime = executeTime;
        this.columns = columns;
    }

    public List<CanalEntry.Column> getColumns() {
        return columns;
    }


    public long getExecuteTime() {
        return executeTime;
    }

    public long getId() {
        return id;
    }

    public long getCommitId() {
        return commitId;
    }
}
