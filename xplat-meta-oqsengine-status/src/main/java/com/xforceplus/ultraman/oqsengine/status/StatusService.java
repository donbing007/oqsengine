package com.xforceplus.ultraman.oqsengine.status;

/**
 * Status Service
 */
public interface StatusService {

    /**
     * get commit Id
     * @return
     */
    Long getCommitId();

    /**
     * save commit id with remote time
     */
    void saveCommitId(Long id);

    /**
     * save commit id with localtime
     * @param id
     * @param timeInMilli
     */
    void saveCommitIdWithLocalTime(Long id, Long timeInMilli);


    Long getCurrentCommitLowBound(Long windowTimeRange);


    Long getCurrentCommitLowBoundWithLocalTime(Long start, Long end);


    StatusMetrics getCurrentStatusMetrics();

}
