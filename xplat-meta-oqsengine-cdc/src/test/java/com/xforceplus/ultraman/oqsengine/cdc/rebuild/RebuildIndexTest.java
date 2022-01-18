package com.xforceplus.ultraman.oqsengine.cdc.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

import com.google.common.collect.Lists;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;

import java.util.Collection;
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
    private int defaultSleepInterval = 3_000;
    private int maxSleepWaitLoops = 100;

    @BeforeEach
    public void before() throws Exception {
        super.init(Lists.newArrayList(
            EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS, EntityGenerateTooBar.SUR_PLUS_ENTITY_CLASS, EntityGenerateTooBar.PREPARE_PAUSE_RESUME_ENTITY_CLASS));
    }

    @AfterEach
    public void after() throws Exception {
        EntityGenerateTooBar.startPos = 1;
        super.destroy();
    }

    /**
     * 正常批次重建.
     */
    @Test
    public void rebuildIndex() throws Exception {
        //  初始化数据
        boolean initOk = initData(
            EntityGenerateTooBar.prepareLongStringEntity(totalSize), EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS);

        Assertions.assertTrue(initOk);
        Thread.sleep(10_000);

        DevOpsTaskInfo taskInfo = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(
            EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS,
            EntityGenerateTooBar.now.minusSeconds(1000),
            EntityGenerateTooBar.now.plusSeconds(1000));

        Optional<TaskHandler> taskHandlerOp =
            RebuildInitialization.getInstance().getTaskExecutor().getActiveTask(EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS);

        Assertions.assertTrue(taskHandlerOp.isPresent());

        check(taskInfo, "rebuildIndex");

        Collection<TaskHandler> taskHandlers =
            RebuildInitialization.getInstance().getTaskExecutor().listAllTasks(new Page());
        Assertions.assertEquals(1, taskHandlers.size());

    }

    private void check(DevOpsTaskInfo devOpsTaskInfo, String errorFunction) throws Exception {
        int wakeUp = 0;
        Optional<TaskHandler> taskHandlerOptional =
            RebuildInitialization.getInstance().getTaskExecutor().taskHandler(devOpsTaskInfo.getMaintainid());

        Assertions.assertTrue(taskHandlerOptional.isPresent());

        TaskHandler taskHandler = taskHandlerOptional.get();

        while (true) {

            if (taskHandler.isDone()) {
                break;
            }

            Assertions.assertFalse(taskHandler.isError());

            wakeUp += sleepForWaitStatusOk(wakeUp, errorFunction);
        }

        Assertions.assertTrue(devOpsTaskInfo.getBatchSize() > 0);
        Assertions.assertTrue(taskHandler.isDone());
        Assertions.assertEquals(ONE_HUNDRED_PERCENT, taskHandler.getProgressPercentage());
    }

    private int sleepForWaitStatusOk(int wakeUp, String errorFunction) throws InterruptedException {
        if (wakeUp >= maxSleepWaitLoops) {
            throw new RuntimeException(String.format("function-[%s] too many wait loops, test failed.", errorFunction));
        }
        Thread.sleep(defaultSleepInterval);
        return 1;
    }

    // 初始化数据
    private boolean initData(IEntity[] entities, IEntityClass entityClass) throws Exception {
        for (int i = 0; i < entities.length; i++) {
            MasterDBInitialization.getInstance().getMasterStorage().build(entities[i], entityClass);
        }
        return true;
    }
}
