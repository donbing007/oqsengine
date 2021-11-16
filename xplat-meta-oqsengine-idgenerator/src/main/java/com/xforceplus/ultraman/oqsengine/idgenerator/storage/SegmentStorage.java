package com.xforceplus.ultraman.oqsengine.idgenerator.storage;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.SQLException;
import java.util.Optional;

/**
 * SegmentStorage.
 *
 * @author leo
 */
public interface SegmentStorage {

    /**
     * build a Segment.
     *
     * @param segmentInfo segmentInfo
     *
     * @return 1:success 0:failed
     *
     * @throws SQLException throw if sql failed
     */
    int build(SegmentInfo segmentInfo) throws SQLException;

    /**
     * update a Segment.
     *
     * @param segmentInfo segmentInfo entity
     *
     * @return 1:success 0:failed
     * @throws SQLException throw if sql failed
     */
    int udpate(SegmentInfo segmentInfo) throws SQLException;

    /**
     * query a Segment by BizType.
     *
     * @param bizType
     *
     * @return 1:success 0:failed
     * @throws SQLException throw if sql failed
     */
    Optional<SegmentInfo> query(String bizType) throws SQLException;


    /**
     * Delete a segment by bizType.
     *
     * @param segmentInfo
     *
     * @return 1: success 0: failed
     * @throws SQLException throw if sql failed
     */
    int delete(SegmentInfo segmentInfo) throws SQLException;

}
