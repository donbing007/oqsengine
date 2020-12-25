package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.devops.DevOpsAbstractContainer;
import com.xforceplus.ultraman.oqsengine.devops.EntityGenerateTooBar;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;

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
public class RebuildIndexTest extends DevOpsAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(RebuildIndexTest.class);

    private int totalSize = 1000;
    private int testResumeCount = 1000;
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
        boolean initOk = initData(prepareLongStringEntity(totalSize));

        Assert.assertTrue(initOk);
        long expectSeconds = totalSize - 5;

        TaskHandler taskHandler = taskExecutor.rebuildIndex(longStringEntityClass,
                EntityGenerateTooBar.now,
                EntityGenerateTooBar.now.plusSeconds(expectSeconds));

        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            wakeUp += sleepForWaitStatusOk(wakeUp, "rebuildIndexSimple");
        }
        Assert.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        //  这里减少5毫米，以判断是否
        Assert.assertEquals(expectSeconds, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        重建 + 删除多余20条记录
     */
    @Test
    public void rebuildIndexDeleteSurPlus() throws Exception {
        int skip = 20;
        boolean initOk = initData(prepareSurPlusNeedDeleteEntity(totalSize), skip);

        Assert.assertTrue(initOk);

        long expectedTasks = totalSize - skip;

        TaskHandler taskHandler = taskExecutor.rebuildIndex(surPlusNeedDeleteEntityClass,
                now,
                now.plusSeconds(totalSize));

        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            wakeUp += sleepForWaitStatusOk(wakeUp, "rebuildIndexDeleteSurPlus");
        }
        Assert.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        //  这里减少20个，以判断是否
        Assert.assertEquals(expectedTasks, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        父子类重建、数据为整个数据集1/2
     */
    @Test
    public void rebuildIndexIncludePrefCref() throws Exception {
        boolean initOk = initData(preparePrefCrefEntity(totalSize));
        Assert.assertTrue(initOk);

        TaskHandler taskHandler = taskExecutor.rebuildIndex(crefEntityClass,
                now, now.plusSeconds(totalSize));

        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            wakeUp += sleepForWaitStatusOk(wakeUp, "rebuildIndexIncludePrefCref");
        }
        Assert.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        //  由于pos的值为 totalSize的两倍，在相通时间内仅有一半
        Assert.assertEquals(totalSize / 2, taskHandler.devOpsTaskInfo().getFinishSize());
    }

    /*
        断点续传功能
     */
    @Test
    public void resumeTest() throws Exception {

        //  初始化数据
        boolean initOk = initData(preparePauseResumeEntity(testResumeCount));

        Assert.assertTrue(initOk);

        TaskHandler taskHandler = taskExecutor.rebuildIndex(pauseResumeEntityClass,
                now, now.plusSeconds(testResumeCount));

        /*
            测试sleep N秒后暂停任务
         */
        int wakeUp = 0;
        while (true) {
            if (taskHandler.getProgressPercentage() == ONE_HUNDRED_PERCENT) {
                break;
            }
            Thread.sleep(10 * 1000);
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
            Thread.sleep(20 * 1000);

            taskHandler = taskExecutor.resumeIndex(pauseResumeEntityClass, taskHandler.devOpsTaskInfo().id(), 0);

            Assert.assertNotNull(taskHandler.devOpsTaskInfo());
        }
    }

    private StorageEntity buildStorageEntity(IEntity v, long txId, long commitId) {
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setId(v.id());
        storageEntity.setEntity(v.entityClass().id());
        storageEntity.setPref(v.family().parent());
        storageEntity.setCref(v.family().child());
        storageEntity.setTime(v.time());
        storageEntity.setMaintainId(v.maintainId());
        storageEntity.setCommitId(commitId);
        storageEntity.setTx(txId);

        return storageEntity;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities) throws Exception {
        for (IEntity entity : entities) {
            masterStorage.build(entity);
            indexStorage.buildOrReplace(buildStorageEntity(entity, txId, commitId), entity.entityValue(), false);
        }
        return true;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, int skip) throws Exception {
        for (int i = 0; i < entities.length; i++) {
            if (i >= skip) {
                masterStorage.build(entities[i]);
            }
            indexStorage.buildOrReplace(buildStorageEntity(entities[i], txId, commitId), entities[i].entityValue(), false);
        }
        return true;
    }

    // 初始化数据
    private boolean initData(List<AbstractMap.SimpleEntry<IEntity, IEntity>> entities) throws Exception {
        for (AbstractMap.SimpleEntry<IEntity, IEntity> entity : entities) {
            IEntity fEntity = entity.getKey();
            masterStorage.build(fEntity);
            indexStorage.buildOrReplace(buildStorageEntity(fEntity, txId, commitId), fEntity.entityValue(), false);

            IEntity cEntity = entity.getValue();
            masterStorage.build(cEntity);
            indexStorage.buildOrReplace(buildStorageEntity(cEntity, txId, commitId), cEntity.entityValue(), false);
        }
        return true;
    }
}
