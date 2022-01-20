package com.xforceplus.ultraman.oqsengine.cdc.rebuild;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

import com.google.common.collect.Lists;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;

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
    private int maxSleepWaitLoops = 3_000;
    private static int batchSize = 10;

    private static ExecutorService asyncThreadPool;

    private boolean ifTest = false;

    @BeforeAll
    public static void beforeAll() {
        asyncThreadPool = new ThreadPoolExecutor(batchSize, batchSize,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1024 * 1000),
            ExecutorHelper.buildNameThreadFactory("task-threads", false));
    }

    @AfterAll
    public static void afterAll() {
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }
    }

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

    @Test
    public void bigBatchRebuild() throws Exception {
        if (!ifTest) {
            return;
        }
        int batchSize = 1024 * 1000;

        //  初始化数据
        boolean initOk = initData(
            EntityGenerateTooBar.prepareLongStringEntity(batchSize), EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS);

        Assertions.assertTrue(initOk);
        Thread.sleep(200_000);

        long start = System.currentTimeMillis();

        DevOpsTaskInfo taskInfo = RebuildInitialization.getInstance().getTaskExecutor().rebuildIndex(
            EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS,
            EntityGenerateTooBar.now.minusSeconds(10000),
            EntityGenerateTooBar.now.plusSeconds(10000));

        Optional<TaskHandler> taskHandlerOp =
            RebuildInitialization.getInstance().getTaskExecutor().getActiveTask(EntityGenerateTooBar.LONG_STRING_ENTITY_CLASS);

        Assertions.assertTrue(taskHandlerOp.isPresent());

        check(taskInfo, "bigBatchRebuild");

        long end = System.currentTimeMillis();

        System.out.println("big batch use time " + (end - start) / 1000 + "s");
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
    private boolean initData(List<IEntity> entities, IEntityClass entityClass) throws Exception {
        SQLMasterStorage storage = MasterDBInitialization.getInstance().getMasterStorage();

        List<List<IEntity>> partitions = Lists.partition(entities, 10000);

        List<List<IEntity>> par = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
            if (par.size() == batchSize) {
                batch(par, storage, entityClass);
                par.clear();
            } else {
                par.add(partitions.get(i));
            }
        }

        if (par.size() > 0) {
            batch(par, storage, entityClass);
        }

        return true;
    }

    private void batch(List<List<IEntity>> partitions, SQLMasterStorage storage, IEntityClass entityClass)
        throws SQLException {
        CountDownLatch countDownLatch = new CountDownLatch(partitions.size());
        List<Future<Boolean>> futures = new ArrayList<>(partitions.size());

        for (int i = 0; i < partitions.size(); i++) {
            final List<IEntity> entityList = partitions.get(i);
            futures.add(
                asyncThreadPool.submit(() -> {
                    return build(countDownLatch, storage, entityClass, entityList);
                })
            );
        }

        try {
            if (!countDownLatch.await(300, TimeUnit.SECONDS)) {
                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }

        for (Future<Boolean> f : futures) {
            try {
                if (!f.get()) {
                    throw new SQLException("failed.");
                }
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private boolean build(CountDownLatch countDownLatch, SQLMasterStorage storage,
                       IEntityClass entityClass, List<IEntity> entityList) {
        try {
            final EntityPackage entityPackage = new EntityPackage();
            entityList.forEach(
                e -> {
                    entityPackage.put(e, entityClass);
                }
            );
            storage.build(entityPackage);
            return true;
        } catch (Exception e) {
           return false;
        } finally {
            countDownLatch.countDown();
        }
    }
}
