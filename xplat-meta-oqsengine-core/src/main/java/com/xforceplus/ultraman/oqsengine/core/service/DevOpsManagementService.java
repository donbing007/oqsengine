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

    Optional<IDevOpsTaskInfo> rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception;

    Optional<IDevOpsTaskInfo> resumeIndex(IEntityClass entityClass, String taskId) throws Exception;

    Collection<IDevOpsTaskInfo> listActiveTasks(Page page) throws SQLException;

    Optional<IDevOpsTaskInfo> getActiveTask(IEntityClass entityClass) throws SQLException;

    Collection<IDevOpsTaskInfo> listAllTasks(Page page) throws SQLException;

    Optional<IDevOpsTaskInfo> SyncTask(String taskId) throws Exception;

    void cancel(String taskId) throws SQLException;
}
