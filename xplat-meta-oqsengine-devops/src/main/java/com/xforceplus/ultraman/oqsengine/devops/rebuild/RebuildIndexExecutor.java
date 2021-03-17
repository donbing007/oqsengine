package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.exception.DevopsTaskExistException;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * desc :
 * name : RebuildIndexExecutor
 *
 * @author : xujia
 * date : 2020/11/24
 * @since : 1.8
 */
public interface RebuildIndexExecutor {

    /**
     * 重新创建索引.
     * 重新创建会让此entityClass进入只读模式.
     * 所以有写入事务都会被拒绝.
     * 如果有正在活动的EntityClass任务,那么后续的相同entityClass调用将造成异常.
     *
     * @param entityClass 目标entityClass.
     * @param start       开始时间.
     * @param end         结束时间.
     * @return 任务表示.
     * @throws DevopsTaskExistException 表示任务已经存在不可能再增加.
     */
    TaskHandler rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end)
            throws Exception;

    /**
     *  索引断点继续，当任务处于失败、取消的状态时，可以继续剩下的任务
     *  从startId处开始继续任务
     */
    TaskHandler resumeIndex(IEntityClass entityClass, String taskId, int currentRecovers)
            throws Exception;

    /**
     * 列出当前活动的任务.
     *
     * @return 任务列表.
     * @page 分页信息.
     */
    Collection<TaskHandler> listActiveTasks(Page page) throws SQLException;

    /**
     * 获取当前活动的指定entityClass的任务.
     *
     * @param entityClass 目标entityClass.
     * @return 目标entityClass的任务.
     */
    Optional<TaskHandler> getActiveTask(IEntityClass entityClass) throws SQLException;

    /**
     * 列出所有历史任务.
     *
     * @param page 分页信息.
     * @return 任务列表.
     */
    Collection<TaskHandler> listAllTasks(Page page) throws SQLException;


    /**
     * 列出任务ID所对应的TaskHandler
     *
     * @param taskId 任务ID.
     * @return 任务列表.
     */
    Optional<TaskHandler> syncTask(String taskId) throws SQLException;

    /**
     * 销毁
     */
    void destroy();
}
