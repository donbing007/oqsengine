package com.xforceplus.ultraman.oqsengine.cdc.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.batch.BatchInit;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.rebuild.EntityGenerateTooBar;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : RebuildIndexTest
 *
 * @author xujia 2020/8/27
 * @since 1.8
 */
public class RebuildIndexTest extends AbstractCdcHelper {

    final Logger logger = LoggerFactory.getLogger(RebuildIndexTest.class);

    private int startPost = 1;
    private int totalSize = 50;
    private int batchSize = totalSize * 1024;

    private int defaultSleepInterval = 3_000;
    private int maxSleepWaitLoops = 3_000;

    @BeforeAll
    public static void beforeAll() {
        BatchInit.init();
    }

    @AfterAll
    public static void afterAll() {
        BatchInit.destroy();
        InitializationHelper.destroy();
    }

    @BeforeEach
    public void before() throws Exception {
        super.init(true, null);
    }

    @AfterEach
    public void after() throws Exception {
        clear(true);
        startPost += totalSize + batchSize;
    }

    /**
     * 正常批次重建.
     */
    @Test
    public void rebuildIndex() throws Exception {
        //  初始化数据
        boolean initOk = BatchInit.initData(
            EntityGenerateTooBar.prepareEntities(startPost, totalSize, EntityClassBuilder.ENTITY_CLASS_2), EntityClassBuilder.ENTITY_CLASS_2, totalSize);

        Assertions.assertTrue(initOk);
        Thread.sleep(10_000);

        DevOpsTaskInfo taskInfo = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(
            EntityClassBuilder.ENTITY_CLASS_2,
            DateTimeValue.toLocalDateTime(EntityGenerateTooBar.startTime),
            DateTimeValue.toLocalDateTime(EntityGenerateTooBar.endTime));

        Thread.sleep(2_000);

        check(taskInfo, "rebuildIndex");

        Collection<TaskHandler> taskHandlers =
            RebuildInitialization.getInstance().getTaskExecutor().listAllTasks(new Page());
        Assertions.assertEquals(1, taskHandlers.size());
    }

    @Test
    @Disabled
    public void bigBatchRebuild() throws Exception {

        long start = System.currentTimeMillis();

        boolean initOk = BatchInit.initData(
            EntityGenerateTooBar.prepareEntities(startPost, batchSize, EntityClassBuilder.ENTITY_CLASS_2), EntityClassBuilder.ENTITY_CLASS_2, batchSize);

        Assertions.assertTrue(initOk);

        DevOpsTaskInfo taskInfo = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(
            EntityClassBuilder.ENTITY_CLASS_2,
            DateTimeValue.toLocalDateTime(EntityGenerateTooBar.startTime - 1000),
            DateTimeValue.toLocalDateTime(EntityGenerateTooBar.endTime + 1000));

        Optional<TaskHandler> taskHandlerOp =
            RebuildInitialization.getInstance().getTaskExecutor().getActiveTask(EntityClassBuilder.ENTITY_CLASS_2);

        Assertions.assertTrue(taskHandlerOp.isPresent());

        check(taskInfo, "bigBatchRebuild");

        long end = System.currentTimeMillis();


        logger.info("big batch {}, use time {} ms", batchSize, end - start);
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

        Assertions.assertTrue(taskHandler.devOpsTaskInfo().getBatchSize() > 0);
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
}
