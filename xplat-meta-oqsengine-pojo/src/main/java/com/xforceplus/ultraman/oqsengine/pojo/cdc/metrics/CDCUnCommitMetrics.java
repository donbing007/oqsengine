package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;

/**
 * desc :
 * name : CDCUnCommitMetrics
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCUnCommitMetrics {
    private LinkedHashSet<Long> unCommitIds;
    private Map<Long, RawEntityValue> unCommitEntityValues;

    public CDCUnCommitMetrics() {
        unCommitIds = new LinkedHashSet<>();
        unCommitEntityValues = new ConcurrentHashMap<>();
    }

    public Map<Long, RawEntityValue> getUnCommitEntityValues() {
        return unCommitEntityValues;
    }

    public void setUnCommitEntityValues(Map<Long, RawEntityValue> unCommitEntityValues) {
        this.unCommitEntityValues = unCommitEntityValues;
    }

    public LinkedHashSet<Long> getUnCommitIds() {
        return unCommitIds;
    }

    public void setUnCommitIds(LinkedHashSet<Long> unCommitIds) {
        this.unCommitIds = unCommitIds;
    }
}
