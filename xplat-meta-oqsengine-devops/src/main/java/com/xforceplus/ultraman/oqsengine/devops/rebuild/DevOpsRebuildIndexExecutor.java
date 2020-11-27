package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.exception.DevopsTaskExistException;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.LockExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.OffsetSnapShot;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.DataQueryIterator;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.ERROR.REINDEX_TIME_OUT_ERROR;
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

    private int taskExecTimeout;
    private int splitPart;
    private int maxQueueSize;
    private int executionTimeout;
    private int updateFrequency;

    //  30s
    private long cacheExpireTime;
    private long cacheMaxSize;

    private int maxRecovers = 5;
    private int pageSize;

    private ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

    private ExecutorService asyncThreadPool;

    private ExecutorService taskThreadPool;

    public static Cache<String, BatchStatus> BATCH_STATUS_CACHE;

    public DevOpsRebuildIndexExecutor(int splitPart, int maxQueueSize, int executionTimeout, int updateFrequency,
                                      long cacheExpireTime, long cacheMaxSize, int taskExecTimeout, int pageSize) {
        this.splitPart = splitPart;
        this.maxQueueSize = maxQueueSize;
        this.executionTimeout = executionTimeout;
        this.updateFrequency = updateFrequency;
        this.taskExecTimeout = taskExecTimeout;

        //  30s
        this.cacheExpireTime = cacheExpireTime;
        this.cacheMaxSize = cacheMaxSize;
        this.pageSize = pageSize;
        init();
    }

    public void init() {
        //  splitPart 默认为10, 最大任务并发数为 splitPart的1/3, 一般为3个
        int initSize = splitPart / 3;

        asyncThreadPool = new ThreadPoolExecutor(initSize, initSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize),
                ExecutorHelper.buildNameThreadFactory("task-threads", false));

        //  每个任务处理时内部开启的多线程最大并发数
        taskThreadPool = new ThreadPoolExecutor(splitPart, splitPart,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize),
                ExecutorHelper.buildNameThreadFactory("reindex-call", false));

        //  任务状态CACHE, 缓存30秒
        BATCH_STATUS_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireTime, TimeUnit.SECONDS)
                .maximumSize(cacheMaxSize)
                .build();
    }

    private boolean offsetCountCheck(int expected, int real, boolean check) {
        if (check) {
            return expected == real;
        }
        return true;
    }

    private Optional<DataQueryIterator> initDataQueryIterator(DevOpsTaskInfo taskInfo, boolean isBuild, OffsetSnapShot offsetSnapShot) throws Exception {

        DataQueryIterator dataQueryIterator = masterStorage.newIterator(taskInfo.getEntityClass(), taskInfo.getStarts(), taskInfo.getEnds(),
                taskThreadPool, executionTimeout, pageSize);

        if (null == dataQueryIterator ||
                !offsetCountCheck(taskInfo.getBatchSize(), dataQueryIterator.size(), null != offsetSnapShot)) {
            if (isBuild) {
                //  entityClass有其他的任务正在执行
                if (NULL_UPDATE == eitherRight(buildTask().apply(taskInfo))) {
                    throw new DevopsTaskExistException("entityClass has another running task, current task will be error end!");
                }
            }
            return Optional.empty();
        }

        Function<DevOpsTaskInfo, Either<SQLException, Integer>> func = null;
        if (isBuild) {
            taskInfo.setBatchSize(dataQueryIterator.size());
            func = buildTask();
        } else {
            if (null != offsetSnapShot) {
                dataQueryIterator.resetCheckPoint(offsetSnapShot);
            } else {
                taskInfo.setFinishSize(EMPTY_COLLECTION_SIZE);
            }
            func = resumeTask();
        }


        //  entityClass有其他的任务正在执行
        if (NULL_UPDATE == eitherRight(func.apply(taskInfo))) {
            throw new DevopsTaskExistException("entityClass has another running task, current task will be error end!");
        }

        return Optional.of(dataQueryIterator);
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

        logger.info("async submit task, tid[{}], entityClass[{}], start[{}], end[{}]",
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
        Optional<IDevOpsTaskInfo> devOpsTaskInfo = sqlTaskStorage.selectUnique(Long.parseLong(taskId));
        if (!devOpsTaskInfo.isPresent()) {
            throw new SQLException("task not exists or not suitable to resume.");
        }

        devOpsTaskInfo.get().resetMessage("task recovering.");
        devOpsTaskInfo.get().resetFailedRecovers(currentRecovers);

        logger.info("async resume task, tid[{}], entityClass[{}], start[{}], end[{}]",
                taskId,
                entityClass.id(),
                devOpsTaskInfo.get().getStarts(),
                devOpsTaskInfo.get().getEnds()
        );
        TaskHandler taskHandler = initResume(entityClass, devOpsTaskInfo.get());

        asyncThreadPool.submit(executed(taskHandler, false));

        return taskHandler;
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
            logger.info("task start, tid[{}], entityClass[{}]", taskHandler.id(), taskHandler.devOpsTaskInfo().getEntity());

            try {
                //  handle
                execute(taskHandler, isBuild);

                if (!done(taskHandler)) {
                    String error = String.format("update task done failed, tid[%s], entityClass[%d]",
                            taskHandler.id(),
                            taskHandler.devOpsTaskInfo().getEntity());
                    logger.error(error);
                    throw new SQLException(error);
                }

                logger.info("reIndex task success, tid[{}], entityClass[{}]",
                        taskHandler.id(),
                        taskHandler.devOpsTaskInfo().getEntity()
                );
                return true;
            } catch (Exception e) {
                try {
                    error(taskHandler, e.getMessage());

                    if (null != e.getMessage() && e.getMessage().equals(REINDEX_TIME_OUT_ERROR.name())) {

                        taskHandler.devOpsTaskInfo().resetFailedRecovers(
                                taskHandler.devOpsTaskInfo().failedRecovers() + INCREMENT);

                        if (taskHandler.devOpsTaskInfo().failedRecovers() <= maxRecovers) {
                            Assert.notNull(
                                    resumeIndex(taskHandler.devOpsTaskInfo().getEntityClass(), taskHandler.id(),
                                            taskHandler.devOpsTaskInfo().failedRecovers()),
                                    String.format("resume task failed, tid : %s", taskHandler.id()));
                        }
                    }
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
            throw new SQLException(String.format("task entity-id not match..., origin tid %d", devOpsTaskInfo.getEntity()));
        }
        devOpsTaskInfo.resetStatus(PENDING.getCode());
        devOpsTaskInfo.resetEntityClass(entityClass);
        TaskHandler taskHandler = new DefaultDevOpsTaskHandler(sqlTaskStorage, devOpsTaskInfo);

        return taskHandler;
    }

    private void execute(TaskHandler taskHandler, boolean isBuild) throws Exception {
        try {
            //  初始化迭代器
            DevOpsTaskInfo devOpsTaskInfo = (DevOpsTaskInfo) taskHandler.devOpsTaskInfo();
            Optional<DataQueryIterator> dataQueryIterator = initDataQueryIterator(devOpsTaskInfo, isBuild, devOpsTaskInfo.getOffsetSnapShot());

            if (dataQueryIterator.isPresent()) {
                //   更新TaskHandler中的状态为RUNNING
                if (NULL_UPDATE == sqlTaskStorage.update(taskHandler.devOpsTaskInfo(), RUNNING)) {
                    //  任务被人为终止
                    devOpsTaskInfo.setStatus(CANCEL.getCode());
                    throw new SQLException("task maybe canceled, operation will intercept immediately!");
                }

                DataQueryIterator iterator = dataQueryIterator.get();

                while (iterator.hasNext()) {
                    //  记录最后成功的snap-shot
                    int lastFinish = devOpsTaskInfo.getFinishSize();
                    OffsetSnapShot lastSnapShot = (null != devOpsTaskInfo.getOffsetSnapShot()) ?
                            (OffsetSnapShot) devOpsTaskInfo.getOffsetSnapShot().clone() : null;

                    List<IEntity> entities = iterator.next();

                    try {
                        devOpsTaskInfo.setOffsetSnapShot(iterator.snapShot());
                        //  重建索引
                        consumer(devOpsTaskInfo, entities);
                    } catch (Exception e) {
                        //  这里当处理失败时回退到上一个保存点
                        devOpsTaskInfo.setFinishSize(lastFinish);
                        devOpsTaskInfo.setOffsetSnapShot(lastSnapShot);
                        throw e;
                    }
                }

                //  任务被人为终止
                if (taskHandler.devOpsTaskInfo().getFinishSize() !=
                        taskHandler.devOpsTaskInfo().getBatchSize()) {
                    //  总完成数量和总任务数不等
                    throw new SQLException("finish size not equal batchSize");
                }
            }
        } catch (Exception e) {
            logger.error("execute failed, message : {}", e.getMessage());
            throw e;
        }
    }

    /*
            这里不使用事务,所有的任务在数据库中以状态DONE标记完成，如果失败，请重新执行ReIndex操作
    */
    private boolean done(TaskHandler taskHandler) throws SQLException {

        //  需要删除旧的索引
        indexStorage.clean(taskHandler.devOpsTaskInfo().getEntity(),
                taskHandler.devOpsTaskInfo().getMaintainid(),
                taskHandler.devOpsTaskInfo().getStarts(),
                taskHandler.devOpsTaskInfo().getEnds());

        taskHandler.devOpsTaskInfo().resetMessage("success");
        boolean isDone = sqlTaskStorage.done(taskHandler.devOpsTaskInfo().getMaintainid()) > NULL_UPDATE;
        if (isDone) {
            ((DevOpsTaskInfo) taskHandler.devOpsTaskInfo()).setStatus(DONE.getCode());
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

    /*
        处理往索引库中ReIndex逻辑(replace index)， 主要是按照线程池大小切分任务总数
        每一格处理大小=threadPoolSize即FutureList tasks=threadPoolSize
     */
    private void consumer(DevOpsTaskInfo taskInfo, List<IEntity> entityList) throws SQLException {
        logger.info("start consumer entity, entity size {}", entityList.size());
        if (EMPTY_COLLECTION_SIZE < entityList.size()) {
            int lastFocus = EMPTY_COLLECTION_SIZE;
            List<List<IEntity>> splitEntity = Lists.partition(entityList, splitPart);

            for (List<IEntity> spl : splitEntity) {
                if (EMPTY_COLLECTION_SIZE < spl.size()) {
                    CountDownLatch countDownLatch = new CountDownLatch(spl.size());
                    List<Future> futures = new ArrayList<>(spl.size());
                    for (IEntity entity : spl) {
                        entity.restMaintainId(taskInfo.getMaintainid());
                        futures.add(taskThreadPool.submit(new ReIndexCallable(countDownLatch, entity)));
                    }

                    try {
                        if (!countDownLatch.await(executionTimeout, TimeUnit.MILLISECONDS)) {
                            for (Future f : futures) {
                                f.cancel(true);
                            }

                            throw new SQLException(REINDEX_TIME_OUT_ERROR.name(), REINDEX_TIME_OUT_ERROR.name());
                        }
                    } catch (InterruptedException e) {
                        throw new SQLException(e.getMessage(), e);
                    }
                    if (EMPTY_COLLECTION_SIZE < futures.size()) {
                        taskInfo.addFinishSize(futures.size());
                    }
                }
                /*
                    这里每100条更新一次数据库finishSize,用以统计百分比, 使用乐观锁，当更新结果为0 说明任务被终止,
                    需要将BatchStatus设置为CANCEL，并终止当前任务
                 */
                if (taskInfo.getFinishSize() - lastFocus > updateFrequency) {
                    if (NULL_UPDATE == sqlTaskStorage.update(taskInfo, RUNNING)) {
                        taskInfo.setStatus(CANCEL.getCode());
                        throw new SQLException("task might be canceled.");
                    }
                    lastFocus = taskInfo.getFinishSize();
                }
            }

            /*
                最后一次检查
             */
            if (lastFocus < taskInfo.getFinishSize()) {
                if (NULL_UPDATE == sqlTaskStorage.update(taskInfo, RUNNING)) {
                    taskInfo.setStatus(CANCEL.getCode());
                    throw new SQLException("task might be canceled.");
                }
            }
        }
    }

    private class ReIndexCallable implements Callable<Integer> {
        private CountDownLatch countDownLatch;
        private IEntity entity;

        public ReIndexCallable(CountDownLatch countDownLatch, IEntity entity) {
            this.countDownLatch = countDownLatch;
            this.entity = entity;
        }

        @Override
        public Integer call() throws Exception {
            try {

                StorageEntity storageEntity = new StorageEntity(
                        entity.id(), entity.entityClass().id(), entity.family().parent(), entity.family().child(),
                        maintainTxId, maintainCommitId, null, null, entity.time());

                storageEntity.setMaintainId(entity.maintainId());

                int reIndex = indexStorage.buildOrReplace(storageEntity, entity.entityValue(), true);
                if (NULL_UPDATE == reIndex) {
                    throw new SQLException("reIndex failed, no one replace.");
                }

                return reIndex;
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
