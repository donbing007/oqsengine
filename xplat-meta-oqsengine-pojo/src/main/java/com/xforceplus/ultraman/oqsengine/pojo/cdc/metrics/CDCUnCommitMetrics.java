package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;



import java.util.LinkedHashSet;

/**
 * desc :.
 * name : CDCUnCommitMetrics
 *
 * @author : xujia 2020/11/7
 * @since : 1.8
 */
public class CDCUnCommitMetrics {
    private LinkedHashSet<Long> unCommitIds;

    public CDCUnCommitMetrics() {
        unCommitIds = new LinkedHashSet<>();
    }

    public LinkedHashSet<Long> getUnCommitIds() {
        return unCommitIds;
    }

    public void setUnCommitIds(LinkedHashSet<Long> unCommitIds) {
        this.unCommitIds = unCommitIds;
    }
}
