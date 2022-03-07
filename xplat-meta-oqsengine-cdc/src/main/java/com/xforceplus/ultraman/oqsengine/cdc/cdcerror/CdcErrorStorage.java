package com.xforceplus.ultraman.oqsengine.cdc.cdcerror;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * cdc 错误信息储存.
 *
 * @author xujia 2020/11/21
 * @since : 1.8
 */
public interface CdcErrorStorage {

    /**
     * build cdc error.
     *
     * @param cdcErrorTask 错误实体类.
     * @return 写入数量.
     * @throws SQLException
     */
    int buildCdcError(CdcErrorTask cdcErrorTask) throws SQLException;

    /**
     * update cdc error status.
     *
     * @param seqNo 序列号.
     * @param fixedStatus 状态.
     * @return 写入数量.
     * @throws SQLException
     */
    int updateCdcErrorStatus(long seqNo, FixedStatus fixedStatus) throws SQLException;

    /**
     * query by condition.
     *
     * @param res 查询条件.
     * @return 错误列表集合.
     * @throws SQLException
     */
    Collection<CdcErrorTask> queryCdcErrors(CdcErrorQueryCondition res) throws SQLException;


    /**
     * query by uni-keys.
     *
     * @param res 主键列表.
     * @return 错误列表集合.
     * @throws SQLException
     */
    Collection<CdcErrorTask> queryCdcErrors(List<String> res) throws SQLException;

    /**
     * 批量写入错误.
     *
     * @param errorTasks 错误列表.
     * @return 插入成功.
     * @throws SQLException
     */
    boolean batchInsert(Collection<CdcErrorTask> errorTasks) throws SQLException;
}
