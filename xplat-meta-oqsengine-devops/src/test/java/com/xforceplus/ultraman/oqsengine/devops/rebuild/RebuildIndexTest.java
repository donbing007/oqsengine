package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.devops.AbstractDevOpsContainer;
import com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.*;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

/**
 * desc :
 * name : RebuildIndexTest
 *
 * @author : xujia
 * date : 2020/8/27
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE})
public class RebuildIndexTest extends AbstractDevOpsContainer {

    private int totalSize = 1024;
    private int testResumeCount = 20000;
    private int defaultSleepInterval = 3_000;
    private int maxSleepWaitLoops = 100;
    long txId = 0;
    long commitId = 0;

    @Before
    public void before() throws Exception {
        start();
    }

    @After
    public void after() throws SQLException {
        startPos = 1;
        clear();
        // 确认没有事务.
        //Assert.assertFalse(transactionManager.getCurrent().isPresent());

        close();
    }


    private void check(TaskHandler taskHandler, String errorFunction) throws InterruptedException {
        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            wakeUp += sleepForWaitStatusOk(wakeUp, errorFunction);
        }

        Assert.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());
    }
    private int sleepForWaitStatusOk(int wakeUp, String errorFunction) throws InterruptedException {
        if (wakeUp >= maxSleepWaitLoops) {
            throw new RuntimeException(String.format("function-[%s] too many wait loops, test failed.", errorFunction));
        }
        Thread.sleep(defaultSleepInterval);
        return 1;
    }

    /*
        正常批次重建
     */
    @Test
    public void rebuildIndexSimple() throws Exception {
        //  初始化数据
        boolean initOk = initData(prepareLongStringEntity(totalSize), LONG_STRING_ENTITY_CLASS);

        Assert.assertTrue(initOk);
        Thread.sleep(1_000);
        long expectSeconds = totalSize - 5;

        TaskHandler taskHandler = taskExecutor.rebuildIndex(LONG_STRING_ENTITY_CLASS,
                EntityGenerateTooBar.now,
                EntityGenerateTooBar.now.plusSeconds(expectSeconds));

        check(taskHandler, "rebuildIndexSimple");

        //  这里减少5，以判断是否
        Assert.assertEquals(expectSeconds, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        重建 + 删除多余20条记录
     */
    @Test
    public void rebuildIndexDeleteSurPlus() throws Exception {
        int skip = 20;
        boolean initOk = initData(prepareSurPlusNeedDeleteEntity(totalSize), SUR_PLUS_ENTITY_CLASS, skip);

        Assert.assertTrue(initOk);

        long expectedTasks = totalSize - skip;

        TaskHandler taskHandler = taskExecutor.rebuildIndex(SUR_PLUS_ENTITY_CLASS,
                now,
                now.plusSeconds(totalSize));

        check(taskHandler, "rebuildIndexDeleteSurPlus");

        //  这里减少20个，以判断是否
        Assert.assertEquals(expectedTasks, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        断点续传功能
     */
    @Test
    public void resumeTest() throws Exception {

        //  初始化数据
        boolean initOk = initData(preparePauseResumeEntity(testResumeCount), PREPARE_PAUSE_RESUME_ENTITY_CLASS, false);

        Assert.assertTrue(initOk);

        TaskHandler taskHandler = taskExecutor.rebuildIndex(PREPARE_PAUSE_RESUME_ENTITY_CLASS,
                now, now.plusSeconds(testResumeCount));

        /*
            测试sleep N秒后暂停任务
         */
        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            Thread.sleep(5 * 1000);
            cancelResumeByCondition(taskHandler.id());
            wakeUp += sleepForWaitStatusOk(wakeUp, "resumeTest");
        }
        Assert.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());
    }

    private void cancelResumeByCondition(String taskId) throws Exception {
        Optional<TaskHandler> task = taskExecutor.syncTask(taskId);
        if (task.isPresent()) {
            TaskHandler taskHandler = task.get();
            taskHandler.cancel();
            Thread.sleep(2 * 1000);

            taskHandler = taskExecutor.resumeIndex(PREPARE_PAUSE_RESUME_ENTITY_CLASS, taskHandler.devOpsTaskInfo().id(), 0);

            if (taskHandler instanceof DefaultDevOpsTaskHandler) {
                Assert.assertNotNull(taskHandler.devOpsTaskInfo());
            }
        }
    }

    private OriginalEntity buildStorageEntity(IEntity v, IEntityClass entityClass, long txId, long commitId) {
        return OriginalEntity.Builder.anOriginalEntity()
                                            .withId(v.id())
                                            .withCommitid(commitId)
                                            .withDeleted(false)
                                            .withCreateTime(v.time())
                                            .withUpdateTime(v.time())
                                            .withTx(txId)
                                            .withOp(OperationType.UPDATE.getValue())
                                            .withOqsMajor(v.major())
                                            .withEntityClass(entityClass)
                                            .withAttributes(toIndexAttrs(v.entityValue()))
                                            .build();
    }


    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass, boolean initIndex) throws Exception {
        List<OriginalEntity> originalEntities = new ArrayList<>();
        for (IEntity entity : entities) {
            masterStorage.build(entity, entityClass);
            if (initIndex) {
                originalEntities.add(buildStorageEntity(entity, entityClass, txId, commitId));
            }
        }
        if (initIndex) {
            indexStorage.saveOrDeleteOriginalEntities(originalEntities);
        }
        return true;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass) throws Exception {
        List<OriginalEntity> originalEntities = new ArrayList<>();
        for (IEntity entity : entities) {
            masterStorage.build(entity, entityClass);
            originalEntities.add(buildStorageEntity(entity, entityClass, txId, commitId));
        }
        indexStorage.saveOrDeleteOriginalEntities(originalEntities);
        return true;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass, int skip) throws Exception {
        List<OriginalEntity> originalEntities = new ArrayList<>();
        for (int i = 0; i < entities.length; i++) {
            if (i >= skip) {
                masterStorage.build(entities[i], entityClass);
            }
            originalEntities.add(buildStorageEntity(entities[i], entityClass, txId, commitId));
        }
        indexStorage.saveOrDeleteOriginalEntities(originalEntities);
        return true;
    }


    private List<Object> toIndexAttrs(IEntityValue value) {

        StorageStrategy storageStrategy;

        List<Object> objects = new ArrayList<>();
        for (IValue logicValue : value.values()) {
            storageStrategy = masterStorageStrategyFactory.getStrategy(logicValue.getField().type());
            StorageValue storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {

                objects.add(AnyStorageValue.ATTRIBUTE_PREFIX + storageValue.storageName());
                objects.add(storageValue.value());

                storageValue = storageValue.next();
            }
        }
        return objects;
    }
}
