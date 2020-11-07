package com.xforceplus.ultraman.oqsengine.cdc.metrics.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.INIT_ID;

/**
 * desc :
 * name : CDCUnCommitMetrics
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCUnCommitMetrics implements Cloneable {
    private long lastUnCommitId;
    private Map<Long, IEntityValue> unCommitEntityValues;

    public CDCUnCommitMetrics() {
        lastUnCommitId = INIT_ID;
        unCommitEntityValues = new ConcurrentHashMap<>();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CDCUnCommitMetrics cdcUnCommitMetrics = new CDCUnCommitMetrics();
        cdcUnCommitMetrics.setLastUnCommitId(this.lastUnCommitId);
        for (Map.Entry<Long, IEntityValue> e : unCommitEntityValues.entrySet()) {
            cdcUnCommitMetrics.getUnCommitEntityValues().put(e.getKey(), (IEntityValue) e.getValue().clone());
        }
        return cdcUnCommitMetrics;
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
}
