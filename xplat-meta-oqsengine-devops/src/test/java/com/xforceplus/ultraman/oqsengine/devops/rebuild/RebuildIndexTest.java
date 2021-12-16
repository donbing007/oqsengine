package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.PREPARE_PAUSE_RESUME_ENTITY_CLASS;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.SUR_PLUS_ENTITY_CLASS;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.now;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.prepareLongStringEntity;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.preparePauseResumeEntity;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.prepareSurPlusNeedDeleteEntity;
import static com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar.startPos;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.devops.DevOpsTestHelper;
import com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.DefaultDevOpsTaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * desc :.
 * name : RebuildIndexTest
 *
 * @author xujia 2020/8/27
 * @since 1.8
 */
public class RebuildIndexTest extends DevOpsTestHelper {

    private int totalSize = 1024;
    private int testResumeCount = 200;
    private int defaultSleepInterval = 3_000;
    private int maxSleepWaitLoops = 100;
    long txId = 0;
    long commitId = 0;

    @BeforeEach
    public void before() throws Exception {
        super.init(Lists.newArrayList(LONG_STRING_ENTITY_CLASS, SUR_PLUS_ENTITY_CLASS, PREPARE_PAUSE_RESUME_ENTITY_CLASS));
    }

    @AfterEach
    public void after() throws Exception {
        startPos = 1;
        super.destroy();
    }


    private void check(TaskHandler taskHandler, String errorFunction) throws InterruptedException {
        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            wakeUp += sleepForWaitStatusOk(wakeUp, errorFunction);
        }

        Assertions.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assertions.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
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

        Assertions.assertTrue(initOk);
        Thread.sleep(1_000);
        long expectSeconds = totalSize - 5;

        TaskHandler taskHandler = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(LONG_STRING_ENTITY_CLASS,
            EntityGenerateTooBar.now,
            EntityGenerateTooBar.now.plusSeconds(expectSeconds));

        check(taskHandler, "rebuildIndexSimple");

        //  这里减少5，以判断是否
        Assertions.assertEquals(expectSeconds, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        重建 + 删除多余20条记录
     */
    @Test
    public void rebuildIndexDeleteSurPlus() throws Exception {
        int skip = 20;
        boolean initOk = initData(prepareSurPlusNeedDeleteEntity(totalSize), SUR_PLUS_ENTITY_CLASS, skip);

        Assertions.assertTrue(initOk);

        long expectedTasks = totalSize - skip;

        TaskHandler taskHandler = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(SUR_PLUS_ENTITY_CLASS,
            now,
            now.plusSeconds(totalSize));

        check(taskHandler, "rebuildIndexDeleteSurPlus");

        //  这里减少20个，以判断是否
        Assertions.assertEquals(expectedTasks, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        断点续传功能
     */
    @Test
    public void resumeTest() throws Exception {

        //  初始化数据
        boolean initOk = initData(preparePauseResumeEntity(testResumeCount), PREPARE_PAUSE_RESUME_ENTITY_CLASS, false);

        Assertions.assertTrue(initOk);

        TaskHandler taskHandler = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(PREPARE_PAUSE_RESUME_ENTITY_CLASS,
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
        Assertions.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assertions.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
            taskHandler.devOpsTaskInfo().getFinishSize());
    }

    private void cancelResumeByCondition(String taskId) throws Exception {
        Optional<TaskHandler> task = RebuildInitialization.getInstance().getTaskExecutor().syncTask(taskId);
        if (task.isPresent()) {
            TaskHandler taskHandler = task.get();
            taskHandler.cancel();
            Thread.sleep(2 * 1000);

            taskHandler =
                RebuildInitialization.getInstance().getTaskExecutor().resumeIndex(PREPARE_PAUSE_RESUME_ENTITY_CLASS, taskHandler.devOpsTaskInfo().id(), 0);

            if (taskHandler instanceof DefaultDevOpsTaskHandler) {
                Assertions.assertNotNull(taskHandler.devOpsTaskInfo());
            }
        }
    }

    private OriginalEntity buildStorageEntity(IEntity v, IEntityClass entityClass, long txId, long commitId)
        throws Exception {
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
            MasterDBInitialization.getInstance().getMasterStorage().build(entity, entityClass);
            if (initIndex) {
                originalEntities.add(buildStorageEntity(entity, entityClass, txId, commitId));
            }
        }
        if (initIndex) {
            IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(originalEntities);
        }
        return true;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass) throws Exception {

        List<OriginalEntity> originalEntities = new ArrayList<>();
        for (IEntity entity : entities) {
            MasterDBInitialization.getInstance().getMasterStorage().build(entity, entityClass);
            originalEntities.add(buildStorageEntity(entity, entityClass, txId, commitId));
        }
        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(originalEntities);
        return true;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass, int skip) throws Exception {
        List<OriginalEntity> originalEntities = new ArrayList<>();
        for (int i = 0; i < entities.length; i++) {
            if (i >= skip) {
                MasterDBInitialization.getInstance().getMasterStorage().build(entities[i], entityClass);
            }
            originalEntities.add(buildStorageEntity(entities[i], entityClass, txId, commitId));
        }
        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(originalEntities);
        return true;
    }


    private List<Object> toIndexAttrs(IEntityValue value) throws Exception {

        StorageStrategy storageStrategy;

        List<Object> objects = new ArrayList<>();
        for (IValue logicValue : value.values()) {
            storageStrategy = MasterDBInitialization.getInstance().getMasterStorageStrategyFactory().getStrategy(logicValue.getField().type());
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
