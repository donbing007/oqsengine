package com.xforceplus.ultraman.oqsengine.status;

import java.util.List;

/**
 * current status metrics
 */
public class StatusMetrics {

    private String lowBound;

    private String upBound;

    private Long size;

    private List<Long> transIds;

    public String getLowBound() {
        return lowBound;
    }

    public void setLowBound(String lowBound) {
        this.lowBound = lowBound;
    }

    public String getUpBound() {
        return upBound;
    }

    public void setUpBound(String upBound) {
        this.upBound = upBound;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<Long> getTransIds() {
        return transIds;
    }

    public void setTransIds(List<Long> transIds) {
        this.transIds = transIds;
    }

    @Override
    public String toString() {
        return "StatusMetrics{" +
            "lowBound='" + lowBound + '\'' +
            ", upBound='" + upBound + '\'' +
            ", size=" + size +
            ", transIds=" + transIds +
            '}';
    }
}
