package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.exception.DevopsTaskExistException;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.*;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.*;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.ERROR.DUPLICATE_KEY_ERROR;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.EitherUtils.eitherRight;

/**
 * desc :
 * name : DevOpsTaskExecutor
 *
 * @author : xujia
 * date : 2020/8/26
 * @since : 1.8
 */
public class DevOpsRebuildIndexExecutor implements RebuildIndexExecutor {
    final Logger logger = LoggerFactory.getLogger(DevOpsRebuildIndexExecutor.class);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Resource
    private SQLTaskStorage sqlTaskStorage;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

    private int maxRecovers = 5;

    private ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

    private ExecutorService asyncThreadPool;

    public static Cache<String, BatchStatus> BATCH_STATUS_CACHE;

    public DevOpsRebuildIndexExecutor(int splitPart, int maxQueueSize, long cacheExpireTime, long cacheMaxSize) {
        //  splitPart 默认为10, 最大任务并发数为 splitPart的1/3, 一般为3个
        int initSize = splitPart / 3;

        asyncThreadPool = new ThreadPoolExecutor(initSize, initSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize),
                ExecutorHelper.buildNameThreadFactory("task-threads", false));

        //  任务状态CACHE, 缓存30秒
        BATCH_STATUS_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireTime, TimeUnit.SECONDS)
                .maximumSize(cacheMaxSize)
                .build();
    }

    public void destroy() {
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }
    }

    private DataIterator<OriginalEntity> initDataQueryIterator(DevOpsTaskInfo taskInfo, boolean isBuild) throws Exception {

        /**
         * 获得迭代器
         */
        DataIterator<OriginalEntity> dataQueryIterator =
                masterStorage.iterator(taskInfo.getEntityClass(), taskInfo.getStarts(), taskInfo.getEnds(), taskInfo.startId());

        if (null == dataQueryIterator) {
            throw new DevopsTaskExistException("has no iterator to rebuild, current task will be error end!");
        }

        Function<DevOpsTaskInfo, Either<SQLException, Integer>> func = null;
        taskInfo.setBatchSize(dataQueryIterator.size());
        taskInfo.setStatus(RUNNING.getCode());
        if (isBuild) {
            taskInfo.resetMessage("TASK PROCESSING");
            func = buildTask();
        } else {
            func = resumeTask();
        }

        if (NULL_UPDATE == eitherRight(func.apply(taskInfo))) {
            if (!isBuild) {
                logger.warn("task {} has finished, un-necessary to reIndex!", taskInfo.getMaintainid());
            }
            return null;
        }

        return dataQueryIterator;
    }

    private Function<DevOpsTaskInfo, Either<SQLException, Integer>> buildTask() {
        return sqlTaskStorage::build;
    }

    private Function<DevOpsTaskInfo, Either<SQLException, Integer>> resumeTask() {
        return sqlTaskStorage::resumeTask;
    }

    @Override
    public TaskHandler rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception {

        //  init
        TaskHandler taskHandler = pending(entityClass, start, end);

        logger.info("async submit task, maintainId {}, entityClass {}, start {}, end {}",
                taskHandler.id(),
                entityClass.id(),
                start,
                end
        );

        asyncThreadPool.submit(executed(taskHandler, true));

        return taskHandler;
    }

    @Override
    public TaskHandler resumeIndex(IEntityClass entityClass, String taskId, int currentRecovers) throws Exception {

        //  entityClass有其他的任务正在执行
        Optional<IDevOpsTaskInfo> devOpsTaskInfoOp = sqlTaskStorage.selectUnique(Long.parseLong(taskId));
        if (devOpsTaskInfoOp.isPresent()) {
            IDevOpsTaskInfo devOpsTaskInfo = devOpsTaskInfoOp.get();
            if (canResumeIndex(devOpsTaskInfo.getStatus())) {

                devOpsTaskInfo.resetStatus(PENDING.getCode());
                devOpsTaskInfo.resetMessage("TASK RECOVERING");
                devOpsTaskInfo.resetFailedRecovers(currentRecovers);

                logger.info("async resume task, maintainId {}, entityClass {}, start {}, end {}",
                        taskId,
                        entityClass.id(),
                        devOpsTaskInfo.getStarts(),
                        devOpsTaskInfo.getEnds()
                );
                TaskHandler taskHandler = initResume(entityClass, devOpsTaskInfo);

                asyncThreadPool.submit(executed(taskHandler, false));

                return taskHandler;
            }

            logger.warn("task {} has been recovered or finished, resumeIndex will be ignore...", taskId);
        }

        throw new SQLException("task not exists or not suitable to resume.");
    }

    private boolean canResumeIndex(int status) {
        return status != RUNNING.getCode() && status != DONE.getCode();
    }

    private class ExecutedCallable implements Callable<Boolean> {

        private TaskHandler taskHandler;
        private boolean isBuild;

        public ExecutedCallable(TaskHandler taskHandler, boolean isBuild) {
            this.taskHandler = taskHandler;
            this.isBuild = isBuild;
        }

        @Override
        public Boolean call() throws Exception {
            logger.info("task start, maintainId {} , entityClass {}", taskHandler.id(), taskHandler.devOpsTaskInfo().getEntity());
            boolean ret = true;
            try {
                //  handle
                ret = execute(taskHandler, isBuild);
                if (!ret) {
                    if (isBuild) {
                        throw new SQLException(
                                String.format("task %s, entityClass has another running task, current task will be ignore..."
                                        , taskHandler.id()), DUPLICATE_KEY_ERROR.name(), DUPLICATE_KEY_ERROR.ordinal());
                    }
                    return true;
                }
                if (!done(taskHandler)) {
                    String error = String.format("update task done failed, maintainId %s, entityClass %d",
                            taskHandler.id(),
                            taskHandler.devOpsTaskInfo().getEntity());
                    logger.error(error);
                    throw new SQLException(error);
                }

                logger.info("reIndex task success, maintainId {}, entityClass {}",
                        taskHandler.id(),
                        taskHandler.devOpsTaskInfo().getEntity()
                );
                return true;
            } catch (Exception e) {
                if (!ret) {
                    throw e;
                }
                logger.warn("reIndex task failed, maintainId {}, entityClass {}",
                        taskHandler.id(),
                        taskHandler.devOpsTaskInfo().getEntity()
                );
                try {
                    error(taskHandler, e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                throw e;
            }
        }
    }

    public ExecutedCallable executed(TaskHandler taskHandler, boolean isBuild) {
        return new ExecutedCallable(taskHandler, isBuild);
    }

    @Override
    public Collection<TaskHandler> listActiveTasks(Page page) throws SQLException {
        Collection<IDevOpsTaskInfo> taskInfoList = sqlTaskStorage.listActives(page);

        return (null != taskInfoList && EMPTY_COLLECTION_SIZE < taskInfoList.size()) ?
                taskInfoList.stream().map(this::newTaskHandler).collect(Collectors.toList()) : new ArrayList<>();
    }

    @Override
    public Optional<TaskHandler> getActiveTask(IEntityClass entityClass) throws SQLException {
        Collection<IDevOpsTaskInfo> taskInfoCollection = sqlTaskStorage.selectActive(entityClass.id());
        if (MAX_ALLOW_ACTIVE < taskInfoCollection.size()) {
            throw new SQLException("more than 1 active task error.");
        }
        if (!taskInfoCollection.isEmpty()) {
            return Optional.of(newTaskHandler(taskInfoCollection.iterator().next()));
        }
        return Optional.empty();
    }

    @Override
    public Collection<TaskHandler> listAllTasks(Page page) throws SQLException {
        Collection<IDevOpsTaskInfo> taskInfoList = sqlTaskStorage.listAll(page);

        return (null != taskInfoList && EMPTY_COLLECTION_SIZE < taskInfoList.size()) ?
                taskInfoList.stream().map(this::newTaskHandler).collect(Collectors.toList()) : new ArrayList<>();
    }

    @Override
    public Optional<TaskHandler> syncTask(String taskId) throws SQLException {
        return sqlTaskStorage.selectUnique(Long.parseLong(taskId)).map(this::newTaskHandler);
    }

    private TaskHandler pending(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) {
        TaskHandler taskHandler = new DefaultDevOpsTaskHandler(sqlTaskStorage,
                new DevOpsTaskInfo(
                        idGenerator.next(),
                        entityClass,
                        start.toInstant(zoneOffset).toEpochMilli(),
                        end.toInstant(zoneOffset).toEpochMilli()));

        return taskHandler;
    }

    private TaskHandler initResume(IEntityClass entityClass, IDevOpsTaskInfo devOpsTaskInfo) throws Exception {
        if (devOpsTaskInfo.getEntity() != entityClass.id()) {
            throw new SQLException(String.format("task entity-id not match..., origin maintainId %d", devOpsTaskInfo.getEntity()));
        }
        devOpsTaskInfo.resetEntityClass(entityClass);
        return new DefaultDevOpsTaskHandler(sqlTaskStorage, devOpsTaskInfo);
    }

    private boolean execute(TaskHandler taskHandler, boolean isBuild) throws Exception {
        try {
            //  初始化迭代器
            DevOpsTaskInfo devOpsTaskInfo = (DevOpsTaskInfo) taskHandler.devOpsTaskInfo();
            DataIterator<OriginalEntity> iterator = initDataQueryIterator(devOpsTaskInfo, isBuild);
            if (null == iterator) {
                return false;
            }

            boolean isFinish = false;
            while (true) {
                //  记录最后成功的snap-shot
                int lastFinish = devOpsTaskInfo.getFinishSize();
                long startId = devOpsTaskInfo.startId();

                List<OriginalEntity> batchEntities = new ArrayList<>();
                for (int i = 0; i < MAX_BATCH_SIZE; i ++) {
                    if (iterator.hasNext()) {
                        OriginalEntity originalEntity = iterator.next();
                        originalEntity.setMaintainid(devOpsTaskInfo.getMaintainid());
                        batchEntities.add(originalEntity);
                    } else {
                        isFinish = true;
                        break;
                    }
                }

                try {
                    consumer(devOpsTaskInfo, batchEntities);
                    if (isFinish) {
                        if (devOpsTaskInfo.getBatchSize() != devOpsTaskInfo.getFinishSize()) {
                            devOpsTaskInfo.setStatus(ERROR.getCode());
                            throw new SQLException(String.format("task batchSize not equal finishSize when iterator is empty, maintainId %d "
                                    , devOpsTaskInfo.getMaintainid()));
                        }
                        break;
                    } else {
                        if (devOpsTaskInfo.getFinishSize() - lastFinish != MAX_BATCH_SIZE) {
                            devOpsTaskInfo.setStatus(ERROR.getCode());
                            throw new SQLException(String.format("task batchSize not equal finishSize, maintainId %d "
                                    , devOpsTaskInfo.getMaintainid()));
                        }
                    }
                } catch (Exception e) {
                    devOpsTaskInfo.resetStartId(startId);
                    devOpsTaskInfo.setFinishSize(lastFinish);
                    throw e;
                }
            }
        } catch (Exception e) {
            logger.error("execute failed, message [{}]", e.getMessage());
            throw e;
        }

        return true;
    }

    /*
            这里不使用事务,所有的任务在数据库中以状态DONE标记完成，如果失败，请重新执行ReIndex操作
    */
    private boolean done(TaskHandler taskHandler) throws SQLException {

        //  需要删除旧的索引
        long total = indexStorage.clean(taskHandler.devOpsTaskInfo().getEntityClass(),
                taskHandler.devOpsTaskInfo().getMaintainid(),
                taskHandler.devOpsTaskInfo().getStarts(),
                taskHandler.devOpsTaskInfo().getEnds());
        logger.debug("clean data fin, maintainId {}, total clean {}", taskHandler.devOpsTaskInfo().getMaintainid(), total);

        taskHandler.devOpsTaskInfo().resetMessage("success");
        boolean isDone = sqlTaskStorage.done(taskHandler.devOpsTaskInfo().getMaintainid()) > NULL_UPDATE;
        if (isDone) {
            logger.info("task done, maintainId {}, finish batchSize {}"
                    , taskHandler.devOpsTaskInfo().getMaintainid(), taskHandler.devOpsTaskInfo().getFinishSize());
            ((DevOpsTaskInfo) taskHandler.devOpsTaskInfo()).setStatus(DONE.getCode());
        } else {
            logger.warn("task done error, task update finish status error, maintainId {}"
                    , taskHandler.devOpsTaskInfo().getMaintainid());
        }
        return isDone;
    }

    private void error(TaskHandler taskHandler, String message) throws SQLException {
        BatchStatus batchStatus = ERROR;
        DevOpsTaskInfo devOpsTaskInfo = (DevOpsTaskInfo) taskHandler.devOpsTaskInfo();

        if (devOpsTaskInfo.isCancel()) {
            batchStatus = CANCEL;
        }

        devOpsTaskInfo.setStatus(batchStatus.getCode());
        devOpsTaskInfo.resetMessage(message);
        sqlTaskStorage.error(devOpsTaskInfo);
    }

    private TaskHandler newTaskHandler(IDevOpsTaskInfo taskInfo) {
        return new DefaultDevOpsTaskHandler(sqlTaskStorage, taskInfo);
    }

    private void consumer(DevOpsTaskInfo taskInfo, List<OriginalEntity> entityList) throws SQLException {

        if (EMPTY_COLLECTION_SIZE < entityList.size()) {
            long startId = entityList.get(entityList.size() - 1).getId();
            logger.info("start consumer entity, maintainId {}, startId {}, entity size {}"
                                    , taskInfo.getMaintainid(), startId, entityList.size());


            //  批量更新
            indexStorage.saveOrDeleteOriginalEntities(entityList);
            taskInfo.addFinishSize(entityList.size());
            taskInfo.resetStartId(startId);
            /*
                更新状态，如果更新失败，则说明当前任务已被cancel
             */
            if (NULL_UPDATE == sqlTaskStorage.update(taskInfo, RUNNING)) {
                taskInfo.setStatus(CANCEL.getCode());
                throw new SQLException(String.format("task might be canceled, maintainId %d", taskInfo.getMaintainid()));
            }
            logger.info("finish consumer entity, maintainId {}, startId {}, entity size {}"
                                            , taskInfo.getMaintainid(), startId, entityList.size());
        } else {
            logger.warn("consumer reach empty entities, ignore, maintainId {} ", taskInfo.getMaintainid());
        }

    }
}
