package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.TaskStorage;
import com.xforceplus.ultraman.oqsengine.devops.repair.CommitIdRepairExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.INIT_ID;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.PENDING;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.RUNNING;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.ERROR.VALIDATION_ERROR;

/**
 * desc :
 * name : DevOpsManagementServiceImpl
 *
 * @author : xujia
 * date : 2020/9/8
 * @since : 1.8
 */
public class DevOpsManagementServiceImpl implements DevOpsManagementService {

    static final Logger logger = LoggerFactory.getLogger(DevOpsManagementServiceImpl.class);

    @Resource
    private RebuildIndexExecutor devOpsRebuildIndexExecutor;

    @Resource
    private TaskStorage sqlTaskStorage;

    @Resource
    private CommitIdRepairExecutor commitIdRepairExecutor;

    @Override
    public Optional<IDevOpsTaskInfo> rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception {
        return Optional.of(devOpsRebuildIndexExecutor.rebuildIndex(entityClass, start, end).devOpsTaskInfo());
    }

    @Override
    public Optional<IDevOpsTaskInfo> resumeRebuild(IEntityClass entityClass, String taskId) throws Exception {
        return Optional.of(devOpsRebuildIndexExecutor.resumeIndex(entityClass, taskId, INIT_ID).devOpsTaskInfo());
    }

    @Override
    public Collection<IDevOpsTaskInfo> listActiveTasks(Page page) throws SQLException {
        Collection<TaskHandler> collections = devOpsRebuildIndexExecutor.listActiveTasks(page);
        if (collections.isEmpty()) {
            return new ArrayList<>();
        }
        return collections.stream().map(TaskHandler::devOpsTaskInfo)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<IDevOpsTaskInfo> getActiveTask(IEntityClass entityClass) throws SQLException {
        return devOpsRebuildIndexExecutor.getActiveTask(entityClass)
            .map(TaskHandler::devOpsTaskInfo);
    }

    @Override
    public Collection<IDevOpsTaskInfo> listAllTasks(Page page) throws SQLException {
        Collection<TaskHandler> collections = devOpsRebuildIndexExecutor.listAllTasks(page);
        if (collections.isEmpty()) {
            return new ArrayList<>();
        }
        return collections.stream().map(TaskHandler::devOpsTaskInfo)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<IDevOpsTaskInfo> syncTask(String taskId) throws SQLException {
        try {
            return sqlTaskStorage.selectUnique(Long.parseLong(taskId));
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    @Override
    public void cancel(String taskId) throws SQLException {
        String message = null;
        Optional<IDevOpsTaskInfo> devOpsTaskInfo = sqlTaskStorage.selectUnique(Long.parseLong(taskId));
        if (devOpsTaskInfo.isPresent()) {
            if (devOpsTaskInfo.get().getStatus() == RUNNING.getCode() ||
                devOpsTaskInfo.get().getStatus() == PENDING.getCode()) {
                if (NULL_UPDATE != sqlTaskStorage.cancel(devOpsTaskInfo.get().getMaintainid())) {
                    logger.info("task {} be canceled.", devOpsTaskInfo.get().getMaintainid());
                    return;
                }
            }
            message = "cancel task failed, task maybe end, please use sync task to check status.";
        } else {
            message = String.format("task %s can not be cancel, taskId is invalid.", taskId);
        }

        logger.error(message);
        throw new SQLException(message, VALIDATION_ERROR.name(), VALIDATION_ERROR.ordinal());
    }

    @Override
    public void removeCommitIds(Long... ids) {
        commitIdRepairExecutor.clean(ids);
    }

    @Override
    public void initNewCommitId(Optional<Long> commitId) throws SQLException {
        commitIdRepairExecutor.repair(commitId);
    }
}
