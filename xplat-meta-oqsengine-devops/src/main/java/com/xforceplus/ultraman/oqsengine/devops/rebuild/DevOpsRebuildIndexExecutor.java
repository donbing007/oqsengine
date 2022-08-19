package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.EMPTY_COLLECTION_SIZE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.MAX_ALLOW_ACTIVE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.DONE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.ERROR;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DefaultDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncThreadPool;

    private static final int BATCH_QUERY_SIZE = 2048;

    private int querySize = BATCH_QUERY_SIZE;

    private int taskSize;

    /**
     * construct.
     */
    public DevOpsRebuildIndexExecutor(int taskSize, int querySize) {
        this.taskSize = taskSize;

        if (querySize > 0) {
            this.querySize = querySize;
        }
    }

    @Override
    public DevOpsTaskInfo rebuildIndex(IEntityClass entityClass, LocalDateTime start, LocalDateTime end)
        throws SQLException {

        //  init
        DefaultDevOpsTaskInfo devOpsTaskInfo = pending(entityClass, start, end);

        logger.info("pending rebuildIndex task, maintainId {}, entityClass {}, start {}, end {}",
            devOpsTaskInfo.id(), entityClass.id(), start, end
        );

        if (NULL_UPDATE == sqlTaskStorage.build(devOpsTaskInfo)) {
            return null;
        }

        asyncThreadPool.submit(() -> {
            handleTask(Collections.singletonList(devOpsTaskInfo));
        });

        return devOpsTaskInfo;
    }

    @Override
    public Collection<DevOpsTaskInfo> rebuildIndexes(Collection<IEntityClass> entityClasses, LocalDateTime start,
                                                     LocalDateTime end) {
        List<DevOpsTaskInfo> devOps = new ArrayList<>();
        for (IEntityClass entityClass : entityClasses) {
            DefaultDevOpsTaskInfo devOpsTaskInfo = pending(entityClass, start, end);

            logger.info("pending rebuildIndex task, maintainId {}, entityClass {}, start {}, end {}",
                devOpsTaskInfo.id(), entityClass.id(), start, end
            );

            try {
                if (NULL_UPDATE == sqlTaskStorage.build(devOpsTaskInfo)) {
                    devOpsTaskInfo.resetMessage("init task failed...");
                    devOpsTaskInfo.resetStatus(ERROR.getCode());
                }
            } catch (Exception e) {
                devOpsTaskInfo.resetMessage("init task failed...");
                devOpsTaskInfo.resetStatus(ERROR.getCode());
            }

            devOps.add(devOpsTaskInfo);
        }

        if (!devOps.isEmpty()) {
            Lists.partition(devOps, taskSize).forEach(
                p -> {
                    asyncThreadPool.submit(() -> {
                        handleTask(p);
                    });
                }
            );
        }

        return devOps;
    }

    private void handleTask(List<DevOpsTaskInfo> devOps) {

        for (DevOpsTaskInfo devOpsTaskInfo : devOps) {

            try {
                //  错误的任务将直接设置为任务失败.
                if (devOpsTaskInfo.getStatus() == ERROR.getCode()) {
                    sqlTaskStorage.error(devOpsTaskInfo);
                    continue;
                }

                int updateFlag = 0;
                int frequency = 10;

                DataIterator<OqsEngineEntity> iterator =
                    masterStorage.iterator(
                        devOpsTaskInfo.getEntityClass(),
                        devOpsTaskInfo.getStarts(),
                        devOpsTaskInfo.getEnds(),
                        devOpsTaskInfo.getStartId(),
                        querySize,
                        true);

                List<OqsEngineEntity> entities = new ArrayList<>();

                boolean isCanceled = false;
                while (iterator.hasNext()) {
                    OqsEngineEntity originalEntity = iterator.next();

                    //  设置maintainId
                    originalEntity.setMaintainid(devOpsTaskInfo.getMaintainid());

                    entities.add(originalEntity);

                    if (entities.size() == querySize) {

                        indexStorage.saveOrDeleteOriginalEntities(entities);

                        devOpsTaskInfo.addBatchSize(entities.size());
                        devOpsTaskInfo.addFinishSize(entities.size());
                        devOpsTaskInfo.setStartId(originalEntity.getId());

                        entities.clear();

                        updateFlag++;
                        //  每拉10次更新一次任务状态.
                        if (updateFlag == frequency) {
                            if (NULL_UPDATE == sqlTaskStorage.update(devOpsTaskInfo)) {
                                isCanceled = true;
                                break;
                            }
                            updateFlag = 0;
                        }
                    }
                }

                if (!isCanceled) {
                    if (!entities.isEmpty()) {
                        indexStorage.saveOrDeleteOriginalEntities(entities);
                        devOpsTaskInfo.addBatchSize(entities.size());
                        devOpsTaskInfo.addFinishSize(entities.size());
                    }

                    devOpsTaskInfo.setStartId(0L);

                    done(devOpsTaskInfo);
                }
            } catch (Exception e) {
                devOpsTaskInfo.resetMessage(e.getMessage());
                //  任务处理失败, 设置任务为失败状态.
                try {
                    sqlTaskStorage.error(devOpsTaskInfo);
                } catch (Exception ex) {
                    //  打印错误，这个异常将被忽略.
                }
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
