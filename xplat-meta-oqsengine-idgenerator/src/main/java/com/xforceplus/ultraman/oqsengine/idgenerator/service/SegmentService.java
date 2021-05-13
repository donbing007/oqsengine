package com.xforceplus.ultraman.oqsengine.idgenerator.service;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.IDModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;

import java.sql.SQLException;

/**
 * @author leo
 * 号段分配服务
 */
public interface SegmentService {
    /**
     * 根据bizType获取下一个SegmentId对象
     * @param bizType
     * @return
     */
    SegmentId getNextSegmentId(String bizType) throws SQLException;

    /**
     * 根据BizType获取ID模式
     * @param bizType
     * @return
     */
    IDModel getIDModel(String bizType);
}
