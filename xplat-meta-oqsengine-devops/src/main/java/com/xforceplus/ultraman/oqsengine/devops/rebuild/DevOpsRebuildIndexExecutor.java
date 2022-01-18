package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.EMPTY_COLLECTION_SIZE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.MAX_ALLOW_ACTIVE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.DONE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.PENDING;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.RUNNING;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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

    public DevOpsRebuildIndexExecutor(int maxQueueSize) {
        asyncThreadPool = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(maxQueueSize),
            ExecutorHelper.buildNameThreadFactory("task-threads", false));
    }

    @Override
    public void destroy() throws Exception {
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }
    }

    @Override
    public DevOpsTaskInfo rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) throws Exception {

        //  init
        DefaultDevOpsTaskInfo devOpsTaskInfo = pending(entityClass, start, end);

        logger.info("pending rebuildIndex task, maintainId {}, entityClass {}, start {}, end {}",
            devOpsTaskInfo.id(), entityClass.id(), start, end
        );

        taskBuild(devOpsTaskInfo);

        return devOpsTaskInfo;
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

    @Override
    public void sync(Map<Long, DevOpsCdcMetrics> devOpsCdcMetrics) throws SQLException {
        //  提交到异步执行.
        asyncThreadPool.submit(() -> {
            devOpsCdcMetrics.forEach(
                (maintainId, devOpsMetrics) -> {
                    Optional<DevOpsTaskInfo> devOpsTaskInfoOp = null;
                    try {
                        devOpsTaskInfoOp = sqlTaskStorage.selectUnique(maintainId);
                    } catch (SQLException ex) {
                        logger.warn("query task exception, maintainId {}.", maintainId);
                        return;
                    }

                    if (devOpsTaskInfoOp.isPresent()) {
                        DevOpsTaskInfo dt = devOpsTaskInfoOp.get();
                        if (dt.isEnd()) {
                            return;
                        }

                        boolean needUpdate = false;

                        if (devOpsMetrics.getSuccess() > 0) {
                            needUpdate = true;
                            dt.addFinishSize(devOpsMetrics.getSuccess());
                        }

                        if (devOpsMetrics.getFails() > 0) {
                            needUpdate = true;
                            dt.addErrorSize(devOpsMetrics.getFails());
                        }

                        if (needUpdate) {
                            //  任务已完成.
                            if (dt.getErrorSize() == 0 && dt.getFinishSize() >= dt.getBatchSize()) {
                                try {
                                    done(dt);
                                } catch (SQLException ex) {
                                    logger.warn("do task-done exception, maintainId {}.", dt.getMaintainid());
                                }
                            }

                            //  任务存在失败数据.
                            else if (dt.getErrorSize() > 0) {
                                try {
                                    dt.resetMessage("task end with error.");
                                    sqlTaskStorage.error(dt);
                                } catch (SQLException ex) {
                                    logger.warn("do task-error exception, maintainId {}.", dt.getMaintainid());
                                }
                            } else {
                                try {
                                    dt.resetStatus(RUNNING.getCode());
                                    sqlTaskStorage.update(dt);
                                } catch (SQLException ex) {
                                    logger.warn("do task-update exception, maintainId {}.", dt.getMaintainid());
                                }
                            }
                        }
                    }
                }
            );
        });
    }

    private DefaultDevOpsTaskInfo pending(IEntityClass entityClass, LocalDateTime start, LocalDateTime end) {
        return new DefaultDevOpsTaskInfo(
            idGenerator.next(),
            entityClass,
            start.toInstant(zoneOffset).toEpochMilli(),
            end.toInstant(zoneOffset).toEpochMilli()
        );
    }

    private boolean taskBuild(DefaultDevOpsTaskInfo taskInfo) throws Exception {
        taskInfo.setStatus(PENDING.getCode());
        taskInfo.resetMessage("TASK INIT");

        if (NULL_UPDATE == sqlTaskStorage.build(taskInfo)) {
            return false;
        }

        //  执行主表更新
        try {
            int rebuildCount =
                masterStorage
                    .rebuild(taskInfo.getEntity(), taskInfo.getMaintainid(), taskInfo.getStarts(), taskInfo.getEnds());

            if (rebuildCount > 0) {
                taskInfo.setBatchSize(rebuildCount);
                taskInfo.resetStatus(RUNNING.getCode());
                taskInfo.resetMessage("TASK PROCESSING");

                sqlTaskStorage.update(taskInfo);
            } else {
                taskInfo.setBatchSize(0);
                taskInfo.resetMessage("TASK END");

                sqlTaskStorage.done(taskInfo);
            }
        } catch (Exception e) {
            taskInfo.resetMessage(e.getMessage());
            sqlTaskStorage.error(taskInfo);
        }

        return true;
    }

    private boolean done(DevOpsTaskInfo devOpsTaskInfo) throws SQLException {

        devOpsTaskInfo.resetMessage("success");
        boolean isDone = sqlTaskStorage.done(devOpsTaskInfo) > NULL_UPDATE;
        if (isDone) {
            logger.info("task done, maintainId {}, finish batchSize {}",
                devOpsTaskInfo.getMaintainid(), devOpsTaskInfo.getFinishSize());
            ((DefaultDevOpsTaskInfo) devOpsTaskInfo).setStatus(DONE.getCode());
        } else {
            logger.warn("task done error, task update finish status error, maintainId {}",
                devOpsTaskInfo.getMaintainid());
        }
        return isDone;
    }

    private TaskHandler newTaskHandler(DevOpsTaskInfo taskInfo) {
        return new DefaultDevOpsTaskHandler(sqlTaskStorage, taskInfo);
    }
}
