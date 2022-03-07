package com.xforceplus.ultraman.oqsengine.pojo.cdc.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import java.io.Serializable;
import java.util.List;

/**
 * desc :.
 * name : RawEntry
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public class RawEntry implements Serializable {
    private EntityClassType entityClassType;
    private String uniKeyPrefix;
    private int pos;
    private long id;
    private long commitId;
    private List<CanalEntry.Column> columns;

    /**
     * 实例化.
     */
    public RawEntry(EntityClassType entityClassType, String uniKeyPrefix, int pos, long id, long commitId, List<CanalEntry.Column> columns) {
        this.entityClassType = entityClassType;
        this.uniKeyPrefix = uniKeyPrefix;
        this.pos = pos;
        this.id = id;
        this.commitId = commitId;
        this.columns = columns;
    }

    public EntityClassType getEntityClassType() {
        return entityClassType;
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

    public long getId() {
        return id;
    }

    public long getCommitId() {
        return commitId;
    }
}
