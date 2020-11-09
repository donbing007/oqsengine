package com.xforceplus.ultraman.oqsengine.cdc.metrics.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.INIT_ID;
import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : CDCUnCommitMetrics
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCUnCommitMetrics {
    private long lastUnCommitId;
    private long lastUnCommitCount;
    private Map<Long, IEntityValue> unCommitEntityValues;

    public CDCUnCommitMetrics() {
        lastUnCommitId = INIT_ID;
        lastUnCommitCount = ZERO;
        unCommitEntityValues = new ConcurrentHashMap<>();
    }

    public long getLastUnCommitId() {
        return lastUnCommitId;
    }

    public void setLastUnCommitId(long lastUnCommitId) {
        this.lastUnCommitId = lastUnCommitId;
    }

    public Map<Long, IEntityValue> getUnCommitEntityValues() {
        return unCommitEntityValues;
    }

    public void setUnCommitEntityValues(Map<Long, IEntityValue> unCommitEntityValues) {
        this.unCommitEntityValues = unCommitEntityValues;
    }

    public long getLastUnCommitCount() {
        return lastUnCommitCount;
    }

    public void setLastUnCommitCount(long lastUnCommitCount) {
        this.lastUnCommitCount = lastUnCommitCount;
    }
}
