package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.exception.DevopsTaskExistException;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.devops.DevOpsCdcMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 重建任务执行器.
 *
 * @author xujia 2020/11/24
 * @since 1.8
 */
public interface RebuildIndexExecutor extends Lifecycle {

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
    DevOpsTaskInfo rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception;

    /**
     * 重新创建索引.
     * 重新创建会让此entityClass进入只读模式.
     * 所以有写入事务都会被拒绝.
     * 如果有正在活动的EntityClass任务,那么后续的相同entityClass调用将造成异常.
     *
     * @param entityClasses 目标entityClasses.
     * @param start       开始时间.
     * @param end         结束时间.
     * @return 任务表示.
     * @throws DevopsTaskExistException 表示任务已经存在不可能再增加.
     */
    Collection<DevOpsTaskInfo> rebuildIndexes(Collection<IEntityClass> entityClasses,
                               LocalDateTime start, LocalDateTime end) throws Exception;

    /**
     * 终止一个任务.
     *
     * @param maintainId maintainId.
     * @return 是否终止.
     * @throws DevopsTaskExistException 表示任务已经存在不可能再增加.
     */
    boolean cancel(long maintainId) throws Exception;

    /**
     * 获取当前maintainId的handler.
     *
     * @param maintainId maintainId.
     * @return handler.
     */
    Optional<TaskHandler> taskHandler(Long maintainId) throws SQLException;

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
     * 同步任务状态.
     *
     * @param devOpsCdcMetrics 维护指标.
     */
    void sync(Map<Long, DevOpsCdcMetrics> devOpsCdcMetrics) throws SQLException;
}
