package com.xforceplus.ultraman.oqsengine.idgenerator.storage;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:49 AM
 */
public interface SegmentStorage {

    /**
     * build a Segment
     * @param segmentInfo
     * @return
     * @throws SQLException
     */
    int build(SegmentInfo segmentInfo) throws SQLException;

    /**
     * update a Segment
     * @param segmentInfo
     * @return
     * @throws SQLException
     */
    int udpate(SegmentInfo segmentInfo) throws SQLException;

    /**
     * query a Segment by BizType
     * @param bizType
     * @return
     * @throws SQLException
     */
    Optional<SegmentInfo> query(String bizType) throws SQLException;

}
