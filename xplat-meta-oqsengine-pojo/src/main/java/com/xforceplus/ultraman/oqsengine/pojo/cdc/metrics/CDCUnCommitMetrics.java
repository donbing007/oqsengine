package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc :
 * name : CDCUnCommitMetrics
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCUnCommitMetrics {
    private long unCommitId;
    private Map<Long, RawEntityValue> unCommitEntityValues;

    public CDCUnCommitMetrics() {
//        unCommitId = INIT_ID;
        unCommitEntityValues = new ConcurrentHashMap<>();
    }

    public Map<Long, RawEntityValue> getUnCommitEntityValues() {
        return unCommitEntityValues;
    }

    public void setUnCommitEntityValues(Map<Long, RawEntityValue> unCommitEntityValues) {
        this.unCommitEntityValues = unCommitEntityValues;
    }

    public long getUnCommitId() {
        return unCommitId;
    }

    public void setUnCommitId(long unCommitId) {
        this.unCommitId = unCommitId;
    }
}