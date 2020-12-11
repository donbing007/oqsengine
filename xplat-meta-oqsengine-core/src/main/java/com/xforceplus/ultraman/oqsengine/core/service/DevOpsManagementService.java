package com.xforceplus.ultraman.oqsengine.core.service;


import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * desc :
 * name : DevOpsManagementService
 *
 * @author : xujia
 * date : 2020/9/8
 * @since : 1.8
 */
public interface DevOpsManagementService {

    /**
     * rebuild index
     */

    /**
     * 重建索引
     * @param entityClass 需要重建的EntityClass
     * @param start       开始时间
     * @param end         结束时间
     * @return IDevOpsTaskInfo 当前任务信息
     * @throws Exception
     */
    Optional<IDevOpsTaskInfo> rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception;

    /**
     * 任务断点继续执行（从失败点checkpoint开始继续往后执行）
     * @param entityClass 需要重建的EntityClass
     * @param taskId      任务ID
     * @return IDevOpsTaskInfo 当前任务信息
     * @throws Exception
     */
    Optional<IDevOpsTaskInfo> resumeRebuild(IEntityClass entityClass, String taskId) throws Exception;

    /**
     * 看出当前活动状态下的任务列表
     * @param page 翻页对象
     * @return Collection<IDevOpsTaskInfo> 当前任务信息列表
     * @throws SQLException
     */
    Collection<IDevOpsTaskInfo> listActiveTasks(Page page) throws SQLException;

    /**
     * 根据entityClass获取当前活动任务信息
     * @param entityClass 需要查看的EntityClass
     * @return
     * @throws SQLException
     */
    Optional<IDevOpsTaskInfo> getActiveTask(IEntityClass entityClass) throws SQLException;

    /**
     * 列出当前所有活动任务
     * @param page 翻页信息
     * @return Collection<IDevOpsTaskInfo> 当前任务信息列表
     * @throws SQLException
     */
    Collection<IDevOpsTaskInfo> listAllTasks(Page page) throws SQLException;

    /**
     * 同步当前任务状态（从数据库->本地）
     * @param taskId 任务ID
     * @return 当前任务信息
     * @throws Exception
     */
    Optional<IDevOpsTaskInfo> syncTask(String taskId) throws Exception;

    /**
     * 取消运行中的任务
     * @param taskId 任务ID
     * @throws SQLException
     */
    void cancel(String taskId) throws SQLException;


    /**
     * repair
     */

    /**
     * 修复小于当前主版本号的oqs产生的数据.
     * 升级过程是一个异常过程,会马上返回并有一个后台任务在运行.
     *
     * @param classes 目标对象信息列表.
     * @throws SQLException
     */
    void entityRepair(IEntityClass... classes) throws SQLException;

    /**
     * 取消正在执行的修复任务.
     */
    void cancelEntityRepair();

    /**
     * 是否已经完成.
     */
    boolean isEntityRepaired();

    /**
        根据id列表清理Redis中的CommitId
     */
    void removeCommitIds(long... ids);

    /**
        修复redis中的commitId，当参数commitId为NULL时，取目前数据库中最大CommitId + 1
     */
    void initNewCommitId(Optional<Long> commitId) throws SQLException;


    /**
     * CDC_ERRORS
     */
}
