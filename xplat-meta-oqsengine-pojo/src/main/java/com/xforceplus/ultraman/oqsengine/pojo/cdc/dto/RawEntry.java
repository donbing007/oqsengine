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
    private String uniKeyPrefix;
    private int pos;
    private long executeTime;
    private long id;
    private long commitId;
    private List<CanalEntry.Column> columns;

    public RawEntry(String uniKeyPrefix, int pos, long id, long commitId, long executeTime, List<CanalEntry.Column> columns) {
        this.uniKeyPrefix = uniKeyPrefix;
        this.pos = pos;
        this.id = id;
        this.commitId = commitId;
        this.executeTime = executeTime;
        this.columns = columns;
    }

    public int getPos() {
        return pos;
    }

    public List<CanalEntry.Column> getColumns() {
        return columns;
    }

    public String getUniKeyPrefix() {
        return uniKeyPrefix;
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
