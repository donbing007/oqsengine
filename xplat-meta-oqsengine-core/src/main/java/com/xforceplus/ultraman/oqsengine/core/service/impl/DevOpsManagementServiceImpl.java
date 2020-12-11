package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.TaskStorage;
import com.xforceplus.ultraman.oqsengine.devops.repair.CommitIdRepairExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.QueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
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

    final static Logger logger = LoggerFactory.getLogger(DevOpsManagementServiceImpl.class);

    @Resource
    private RebuildIndexExecutor devOpsRebuildIndexExecutor;

    @Resource(name = "callRebuildThreadPool")
    private ExecutorService worker;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private EntityManagementService entityManagementService;

    private Map<Long, Future> taskFutures;

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
        if (null != message) {
            throw new SQLException(message, VALIDATION_ERROR.name(), VALIDATION_ERROR.ordinal());
        }

    }

    @Override
    public synchronized void entityRepair(IEntityClass... classes) throws SQLException {
        if (classes.length == 0) {
            return;
        }

        if (taskFutures == null) {
            taskFutures = new HashMap();
        } else {
            taskFutures.clear();
        }

        for (IEntityClass c : classes) {
            // 只处理子类.
            if (c.extendEntityClass() != null) {
                taskFutures.put(c.id(), worker.submit(
                        new RepairTask(
                                masterStorage.newIterator(c, 0, Long.MAX_VALUE, worker, 0, 100),
                                entityManagementService, (cid) -> taskFutures.remove(cid)
                        )
                ));
            }
        }
    }

    @Override
    public synchronized void cancelEntityRepair() {
        if (taskFutures != null) {
            taskFutures.values().stream().map(f -> f.cancel(true));
        }
        taskFutures = null;
    }

    @Override
    public synchronized boolean isEntityRepaired() {
        return taskFutures == null || taskFutures.isEmpty();
    }

    @Override
    public void removeCommitIds(long... ids) {
        commitIdRepairExecutor.clean(ids);
    }

    @Override
    public void initNewCommitId(Optional<Long> commitId) throws SQLException {
        commitIdRepairExecutor.repair(commitId);
    }

    static class RepairTask implements Runnable {

        private QueryIterator dataQueryIterator;
        private EntityManagementService entityManagementService;
        private long dealSize;
        private Consumer<Long> callback;
        private IEntityClass entityClass;

        public RepairTask(
                QueryIterator dataQueryIterator, EntityManagementService entityManagementService, Consumer<Long> callback) {
            this.dataQueryIterator = dataQueryIterator;
            this.entityManagementService = entityManagementService;
            this.callback = callback;
        }

        @Override
        public void run() {
            List<IEntity> entities = null;
            while (dataQueryIterator.hasNext()) {
                try {
                    entities = dataQueryIterator.next();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    return;
                }

                for (IEntity entity : entities) {
                    if (entityClass == null) {
                        entityClass = entity.entityClass();
                    }

                    try {
                        entityManagementService.replace(entity);
                        dealSize++;

                        logger.info("Repair schedule: entityClass {}, {}/{}.", entity.entityClass().code(),
                                dealSize, dataQueryIterator.size());

                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            callback.accept(entityClass.id());
        }
    }
}
