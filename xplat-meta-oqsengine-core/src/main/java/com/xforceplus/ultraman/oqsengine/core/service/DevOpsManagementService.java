package com.xforceplus.ultraman.oqsengine.core.service;


import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.QueryConditionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * devops 的接口定义.
 *
 * @author xujia 2020/9/8
 * @since 1.8
 */
public interface DevOpsManagementService {

    /**
     * 重建索引.
     *
     * @param entityClass 需要重建的EntityClass
     * @param start       开始时间
     * @param end         结束时间
     * @return IDevOpsTaskInfo 当前任务信息
     * @throws Exception 执行发生异常.
     */
    Optional<DevOpsTaskInfo> rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end)
        throws Exception;

    /**
     * 任务断点继续执行（从失败点checkpoint开始继续往后执行.
     *
     * @param entityClass 需要重建的EntityClass
     * @param taskId      任务ID
     * @return IDevOpsTaskInfo 当前任务信息
     */
    Optional<DevOpsTaskInfo> resumeRebuild(IEntityClass entityClass, String taskId) throws Exception;

    /**
     * 看出当前活动状态下的任务列表.
     *
     * @param page 翻页对象
     * @return 当前任务信息列表.
     */
    Collection<DevOpsTaskInfo> listActiveTasks(Page page) throws SQLException;

    /**
     * 根据entityClass获取当前活动任务信息.
     *
     * @param entityClass 需要查看的EntityClass
     * @return 信息实例.
     */
    Optional<DevOpsTaskInfo> getActiveTask(IEntityClass entityClass) throws SQLException;

    /**
     * 列出当前所有活动任务.
     *
     * @param page 翻页信息
     * @return 当前任务信息列表
     */
    Collection<DevOpsTaskInfo> listAllTasks(Page page) throws SQLException;

    /**
     * 同步当前任务状态（从数据库->本地）.
     *
     * @param taskId 任务ID
     * @return 当前任务信息
     */
    Optional<DevOpsTaskInfo> syncTask(String taskId) throws Exception;

    /**
     * 取消运行中的任务.
     *
     * @param taskId 任务ID
     */
    void cancel(String taskId) throws SQLException;

    /**
     * 根据id列表清理Redis中的CommitId.
     */
    void removeCommitIds(Long... ids);

    /**
     * 修复redis中的commitId，当参数commitId为NULL时，取目前数据库中最大CommitId + 1.
     */
    void initNewCommitId(Optional<Long> commitId) throws SQLException;

    /**
     * 执行修复CDC批次.
     */
    boolean cdcSendErrorRecover(long seqNo, String recoverStr) throws SQLException;

    /**
     * 更新某条状态为修复完毕.
     */
    boolean cdcUpdateStatus(long seqNo, FixedStatus fixedStatus) throws SQLException;

    /**
     * 查询CDC错误.
     */
    Collection<CdcErrorTask> queryCdcError(CdcErrorQueryCondition cdcErrorQueryCondition) throws SQLException;

    /**
     * 主键查询CDC错误.
     */
    Optional<CdcErrorTask> queryOne(long seqNo) throws SQLException;

    /**
     * 获取当前commitId的范围.
     */
    long[] rangeOfCommitId();

    /**
     * 删除比传入commitId小的所有commitId.
     */
    void cleanLessThan(long id);

    /**
     * 查询错误.
     */
    Collection<ErrorStorageEntity> selectErrors(QueryErrorCondition errorCondition) throws SQLException;
}
