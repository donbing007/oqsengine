package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
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
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.*;
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

    @Resource(name = "callRebuildThreadPool")
    private ExecutorService worker;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private EntityManagementService entityManagementService;

    private Map<Long, Future> taskFutures;

    private Map<Long, IDevOpsTaskInfo> taskInfoMap;

    @Resource
    private TaskStorage sqlTaskStorage;

    @Resource
    private CommitIdRepairExecutor commitIdRepairExecutor;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

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
    public synchronized void entityRepair(IEntityClass... classes) throws SQLException {
        if (classes.length == 0) {
            return;
        }

        if (null == taskFutures) {
            taskFutures = new HashMap();
        } else {
            taskFutures.clear();
        }

        if (null == taskInfoMap) {
            taskInfoMap = new HashMap<>();
        }

        for (IEntityClass c : classes) {
            if (c.extendEntityClass() != null && taskInfoMap.containsKey(c.id())) {
                throw new SQLException(String.format("must cancel entityClass task before redo repair. entity : %d", c.id()));
            }
        }

        for (IEntityClass c : classes) {
            // 只处理子类.
            if (c.extendEntityClass() != null) {
                QueryIterator queryIterator = masterStorage.newIterator(c, 0, Long.MAX_VALUE, worker, 30 * 1000, 100);
                if (null != queryIterator) {
                    IDevOpsTaskInfo devOpsTaskInfo = new DevOpsTaskInfo(idGenerator.next(), c, 0, Long.MAX_VALUE);
                    devOpsTaskInfo.setBatchSize(queryIterator.size());
                    taskInfoMap.put(c.id(), devOpsTaskInfo);
                    taskFutures.put(c.id(), worker.submit(
                            new RepairTask(queryIterator, devOpsTaskInfo,
                                    entityManagementService, (cid) -> taskFutures.remove(cid)
                            )
                    ));
                }
            }
        }
    }

    @Override
    public synchronized void cancelEntityRepair(Long... ids) {
        for (Long id : ids) {
            if (null != taskFutures) {
                Future f = taskFutures.remove(id);
                if (null != f) {
                    f.cancel(true);
                }
            }

            if (null != taskInfoMap) {
                IDevOpsTaskInfo devOpsTaskInfo = taskInfoMap.get(id);
                if (null != devOpsTaskInfo) {
                    devOpsTaskInfo.resetStatus(CANCEL.getCode());
                }
            }
        }
    }

    @Override
    public void clearRepairedInfos(Long... ids) {
        if (null != taskInfoMap) {
            if (null != ids && ids.length > 0) {
                for (Long id : ids) {
                    taskInfoMap.remove(id);
                }
            } else {
                taskInfoMap.clear();
                taskInfoMap = null;
            }
        }
    }

    @Override
    public Collection<IDevOpsTaskInfo> repairedInfoList(Long... ids) {
        List<IDevOpsTaskInfo> devOpsTaskInfos = new ArrayList<>();
        if (null != taskInfoMap) {
            if (null != ids && ids.length > 0) {
                for (Long id : ids) {
                    IDevOpsTaskInfo devOpsTaskInfo = taskInfoMap.get(id);
                    if (null != devOpsTaskInfo) {
                        devOpsTaskInfos.add(devOpsTaskInfo);
                    }
                }
            } else {
                taskInfoMap.entrySet().forEach(
                        entry -> {
                            devOpsTaskInfos.add(entry.getValue());
                        }
                );
            }
        }
        return devOpsTaskInfos;
    }

    @Override
    public synchronized boolean isEntityRepaired(Long... ids) {
        if (null != taskInfoMap) {
            List<Long> notReady = new ArrayList<>();
            List<Long> readyTask = new ArrayList<>();
            for (Long id : ids) {
                IDevOpsTaskInfo devOpsTaskInfo = taskInfoMap.get(id);
                if (null != devOpsTaskInfo && !devOpsTaskInfo.isDone()) {
                    notReady.add(id);
                } else {
                    readyTask.add(id);
                }
            }
            logger.debug("ready tasks finish, ids : {}, not ready tasks finish, ids : {}"
                    , readyTask.toString(), notReady.toString());
            return notReady.isEmpty();
        }
        logger.debug("taskInfoMap is empty");
        return true;
    }
    @Override
    public void removeCommitIds(Long... ids) {
        commitIdRepairExecutor.clean(ids);
    }

    @Override
    public void initNewCommitId(Optional<Long> commitId) throws SQLException {
        commitIdRepairExecutor.repair(commitId);
    }

    static class RepairTask implements Runnable {

        private QueryIterator dataQueryIterator;
        private IDevOpsTaskInfo devOpsTaskInfo;
        private EntityManagementService entityManagementService;
        private int dealSize;
        private Consumer<Long> callback;
        private IEntityClass entityClass;

        public RepairTask(
                QueryIterator dataQueryIterator, IDevOpsTaskInfo devOpsTaskInfo, EntityManagementService entityManagementService, Consumer<Long> callback) {
            this.dataQueryIterator = dataQueryIterator;
            this.devOpsTaskInfo = devOpsTaskInfo;
            this.entityManagementService = entityManagementService;
            this.callback = callback;
            this.dealSize = 0;
        }

        @Override
        public void run() {
            List<IEntity> entities = null;
            while (dataQueryIterator.hasNext()) {
                try {
                    entities = dataQueryIterator.next();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    devOpsTaskInfo.resetStatus(ERROR.getCode());
                    devOpsTaskInfo.resetMessage(ex.getMessage());
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
                        devOpsTaskInfo.resetStatus(ERROR.getCode());
                        devOpsTaskInfo.resetMessage(e.getMessage());
                        logger.error(e.getMessage(), e);
                        return;
                    }
                }
            }
            devOpsTaskInfo.resetStatus(DONE.getCode());
            devOpsTaskInfo.setFinishSize(dealSize);
            callback.accept(entityClass.id());
        }
    }
}
