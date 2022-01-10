package com.xforceplus.ultraman.oqsengine.idgenerator.service;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.IDModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import java.sql.SQLException;

/**
 * 号段分配服务.
 *
 * @author leo
 */
public interface SegmentService {

    /**
     * 根据bizType获取下一个SegmentId对象.
     *
     * @param bizType 业务类型标签
     * @return SegmentId
     */
    SegmentId getNextSegmentId(String bizType) throws SQLException;

    /**
     * 根据BizType获取ID模式.
     *
     * @param bizType 业务类型标签
     * @return IDModel
     */
    IDModel getIDModel(String bizType);


    /**
     * 根据BizType获取配置信息.
     *
     * @param bizType 业务类型标签
     * @return Segment 配置.
     */
    SegmentInfo getSegmentInfo(String bizType);


    /**
     * 根据最新的patternKey去重置号段.
     *
     * @param patternKey pattern前缀
     * @param bizType    业务类型标签
     * @return true if successfully reset otherwise false
     */
    boolean resetSegment(String bizType, String patternKey) throws SQLException;
}
