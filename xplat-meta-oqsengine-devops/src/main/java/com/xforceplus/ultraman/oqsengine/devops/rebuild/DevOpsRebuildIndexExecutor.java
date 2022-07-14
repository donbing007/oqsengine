package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.EMPTY_COLLECTION_SIZE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.MAX_ALLOW_ACTIVE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.DONE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.ERROR;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.RUNNING;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.thread.PollingThreadExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DefaultDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.pojo.devops.DevOpsCdcMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 重建索引处理逻辑.
 *
 * @author : j.xu 2020/8/26.
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

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator idGenerator;

    private ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

    private ExecutorService asyncThreadPool;

    private PollingThreadExecutor executor;

    private Map<Long, List<DevOpsCdcMetrics>> tasks;

    /**
     * construct.
     */
    public DevOpsRebuildIndexExecutor(int taskSize, int maxQueueSize) {
        asyncThreadPool = new ThreadPoolExecutor(taskSize, taskSize,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(maxQueueSize),
            ExecutorHelper.buildNameThreadFactory("task-threads", false));

        tasks = new ConcurrentHashMap<>();

        executor = new PollingThreadExecutor(
            "taskHandler",
            1,
            TimeUnit.SECONDS, 5,
            (n) -> taskUpdate(),
            null);

        executor.start();
    }

    @PreDestroy
    public void stop() {
        if (null != executor) {
            executor.stop();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }
    }

    @Override
    public Collection<DevOpsTaskInfo> rebuildIndexes(Collection<IEntityClass> entityClasses, LocalDateTime start,
                                                     LocalDateTime end) throws Exception {
        List<DevOpsTaskInfo> devOps = new ArrayList<>();
        List<DevOpsTaskInfo> errorTasks = new ArrayList<>();
        for (IEntityClass entityClass : entityClasses) {
            DefaultDevOpsTaskInfo devOpsTaskInfo = pending(entityClass, start, end);

            logger.info("pending rebuildIndex task, maintainId {}, entityClass {}, start {}, end {}",
                devOpsTaskInfo.id(), entityClass.id(), start, end
            );
            try {
                if (NULL_UPDATE == sqlTaskStorage.build(devOpsTaskInfo)) {
                    devOpsTaskInfo.resetMessage("init task failed...");
                    devOpsTaskInfo.resetStatus(ERROR.getCode());
                    errorTasks.add(devOpsTaskInfo);
                } else {
                    devOps.add(devOpsTaskInfo);
                }
            } catch (Exception e) {
                devOpsTaskInfo.resetMessage("init task failed...");
                devOpsTaskInfo.resetStatus(ERROR.getCode());
                errorTasks.add(devOpsTaskInfo);
            }
        }

        asyncThreadPool.submit(() -> {
            devOps.forEach(this::handleTask);
        });

        devOps.addAll(errorTasks);

        return devOps;
    }

    @Override
    public DevOpsTaskInfo rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception {

        //  init
        DefaultDevOpsTaskInfo devOpsTaskInfo = pending(entityClass, start, end);

        logger.info("pending rebuildIndex task, maintainId {}, entityClass {}, start {}, end {}",
            devOpsTaskInfo.id(), entityClass.id(), start, end
        );

        if (NULL_UPDATE == sqlTaskStorage.build(devOpsTaskInfo)) {
            return null;
        }

        asyncThreadPool.submit(() -> {
            handleTask(devOpsTaskInfo);
        });

        return devOpsTaskInfo;
    }

    private void handleTask(DevOpsTaskInfo devOpsTaskInfo) {
        try {
            //  执行主表更新
            int rebuildCount =
                masterStorage.rebuild(devOpsTaskInfo.getEntityClass(), devOpsTaskInfo.getMaintainid(),
                    devOpsTaskInfo.getStarts(), devOpsTaskInfo.getEnds());

            Optional<DevOpsTaskInfo> devOpsTaskInfoOp;
            BatchStatus batchStatus = RUNNING;
            try {
                devOpsTaskInfoOp = sqlTaskStorage.selectUnique(devOpsTaskInfo.getMaintainid());
                if (devOpsTaskInfoOp.isPresent() && devOpsTaskInfoOp.get().getFinishSize() == rebuildCount) {
                    batchStatus = DONE;
                }
            } catch (SQLException ex) {
                logger.warn("query task exception, maintainId {}.", devOpsTaskInfo.getMaintainid());
            }

            if (rebuildCount > 0) {
                devOpsTaskInfo.setBatchSize(rebuildCount);
                devOpsTaskInfo.resetStatus(batchStatus.getCode());
                devOpsTaskInfo.resetMessage("TASK PROCESSING");
            } else {
                devOpsTaskInfo.setBatchSize(0);
                devOpsTaskInfo.resetStatus(DONE.getCode());
                devOpsTaskInfo.resetMessage("TASK END");
            }

            if (batchStatus == DONE && rebuildCount > 0) {
                done(devOpsTaskInfo);
            } else {
                sqlTaskStorage.update(devOpsTaskInfo);
            }
        } catch (Exception e) {
            devOpsTaskInfo.resetMessage(e.getMessage());

            try {
                sqlTaskStorage.error(devOpsTaskInfo);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean cancel(long maintainId) throws Exception {
        return sqlTaskStorage.cancel(maintainId) > 0;
    }

    @Override
    public Optional<TaskHandler> taskHandler(Long maintainId) throws SQLException {
        return sqlTaskStorage.selectUnique(maintainId).map(this::newTaskHandler);
    }

    @Override
    public Collection<TaskHandler> listActiveTasks(Page page) throws SQLException {
        Collection<DevOpsTaskInfo> taskInfoList = sqlTaskStorage.listActives(page);

        return (null != taskInfoList && EMPTY_COLLECTION_SIZE < taskInfoList.size())
            ? taskInfoList.stream().map(this::newTaskHandler).collect(Collectors.toList()) : new ArrayList<>();
    }

    @Override
    public Optional<TaskHandler> getActiveTask(IEntityClass entityClass) throws SQLException {
        Collection<DevOpsTaskInfo> taskInfoCollection = sqlTaskStorage.selectActive(entityClass.id());
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
        Collection<DevOpsTaskInfo> taskInfoList = sqlTaskStorage.listAll(page);

        return (null != taskInfoList && EMPTY_COLLECTION_SIZE < taskInfoList.size())
            ? taskInfoList.stream().map(this::newTaskHandler).collect(Collectors.toList()) : new ArrayList<>();
    }

    //  提交到异步执行.
    private void taskUpdate() {
        Set<Long> keys = tasks.keySet();

        if (keys.isEmpty()) {
            return;
        }

        keys.forEach(
            key -> {
                List<DevOpsCdcMetrics> internalTasks = tasks.remove(key);
                if (null != internalTasks && !internalTasks.isEmpty()) {
                    //  合并数据.
                    DevOpsCdcMetrics devOps = metricsMerge(internalTasks);

                    Optional<DevOpsTaskInfo> devOpsTaskInfoOp;
                    try {
                        devOpsTaskInfoOp = sqlTaskStorage.selectUnique(key);
                    } catch (SQLException ex) {
                        logger.warn("query task exception, maintainId {}.", key);
                        return;
                    }

                    if (devOpsTaskInfoOp.isPresent()) {
                        DevOpsTaskInfo dt = devOpsTaskInfoOp.get();
                        if (dt.isEnd()) {
                            if (dt.isDone() && dt.getStatus() != DONE.getCode()) {
                                try {
                                    done(dt);
                                } catch (Exception e) {
                                    logger.warn("done task exception, maintainId {}, message {}.", key,
                                        e.getMessage());
                                }
                            }
                            return;
                        }

                        boolean needUpdate = false;

                        if (devOps.getSuccess() > 0) {
                            needUpdate = true;
                            dt.resetIncrementSize(devOps.getSuccess());

                            //  完成数量
                            dt.addFinishSize(devOps.getSuccess());
                        }

                        if (devOps.getFails() > 0) {
                            needUpdate = true;
                            dt.addErrorSize(devOps.getFails());
                        }

                        if (needUpdate) {
                            //  任务存在失败数据.
                            if (dt.getErrorSize() > 0) {
                                try {
                                    dt.resetMessage("task end with error.");
                                    sqlTaskStorage.error(dt);
                                } catch (SQLException ex) {
                                    logger.warn("do task-error exception, maintainId {}.",
                                        dt.getMaintainid());
                                }
                            } else {
                                try {
                                    //  任务已完成.
                                    if (dt.getBatchSize() > 0 && dt.getBatchSize() - dt.getFinishSize() <= 0) {
                                        done(dt);
                                    } else {
                                        dt.resetStatus(RUNNING.getCode());
                                        sqlTaskStorage.update(dt);
                                    }
                                } catch (SQLException ex) {
                                    logger.warn("do task-update exception, maintainId {}.",
                                        dt.getMaintainid());
                                }
                            }
                        }
                    }
                }
            }
        );
    }

    private DevOpsCdcMetrics metricsMerge(List<DevOpsCdcMetrics> devOpsCdcMetrics) {
        DevOpsCdcMetrics devOps = new DevOpsCdcMetrics();
        devOpsCdcMetrics.forEach(
            devOpsMetric -> {
                devOps.addFails(devOpsMetric.getFails());
                devOps.addSuccess(devOpsMetric.getSuccess());
            }
        );

        return devOps;
    }

    @Override
    public void sync(Map<Long, DevOpsCdcMetrics> devOpsCdcMetrics) throws SQLException {
        devOpsCdcMetrics.forEach(
            (k, v) -> {
                tasks.computeIfAbsent(k,
                    s -> {
                        return new ArrayList<>();
                    }).add(v);
            }
        );
    }

    private DefaultDevOpsTaskInfo pending(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) {
        return new DefaultDevOpsTaskInfo(
            idGenerator.next(),
            entityClass,
            start.toInstant(zoneOffset).toEpochMilli(),
            end.toInstant(zoneOffset).toEpochMilli()
        );
    }

    private boolean done(DevOpsTaskInfo devOpsTaskInfo) throws SQLException {
        //  删除index脏数据
        try {
            indexStorage.clean(devOpsTaskInfo.getEntity(), devOpsTaskInfo.getMaintainid(),
                devOpsTaskInfo.getStarts(), devOpsTaskInfo.getEnds());
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        //  更新状态为完成
        devOpsTaskInfo.resetMessage("success");

        if (sqlTaskStorage.done(devOpsTaskInfo) > NULL_UPDATE) {
            logger.info("task done, maintainId {}, finish batchSize {}",
                devOpsTaskInfo.getMaintainid(), devOpsTaskInfo.getFinishSize());
            ((DefaultDevOpsTaskInfo) devOpsTaskInfo).setStatus(DONE.getCode());
        } else {
            logger.warn("task done error, task update finish status error, maintainId {}",
                devOpsTaskInfo.getMaintainid());
        }
        return true;
    }

    private TaskHandler newTaskHandler(DevOpsTaskInfo taskInfo) {
        return new DefaultDevOpsTaskHandler(sqlTaskStorage, taskInfo);
    }
}
