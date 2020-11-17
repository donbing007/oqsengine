package com.xforceplus.ultraman.oqsengine.status;

import java.util.List;

/**
 * Status Service
 */
public interface StatusService {

    /**
     * get commit Id
     *
     * @return
     */
    @Deprecated
    Long getCommitId();

    /**
     * save commit id with remote time
     */
    void saveCommitId(Long id);

    /**
     * save commit id with localtime
     *
     * @param id
     * @param timeInMilli
     */
    void saveCommitIdWithLocalTime(Long id, Long timeInMilli);

    /**
     * invalidate id
     *
     * @param ids
     */
    void invalidateIds(List<Long> ids);

    Long getCurrentCommitLowBound(Long windowTimeRange);

    Long getCurrentCommitLowBoundWithLocalTime(Long start, Long end);

    StatusMetrics getCurrentStatusMetrics();

    void saveCDCMetrics(String key, String cdcMetricsJson);

    String getCDCMetrics(String key);

    void closeConnection();
}
