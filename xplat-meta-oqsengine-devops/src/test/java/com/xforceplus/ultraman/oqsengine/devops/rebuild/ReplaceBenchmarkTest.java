package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.DevopsRebuildIndex;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;


/**
 * desc :
 * name : ReplaceBenchmarkTest
 *
 * @author : xujia
 * date : 2020/9/23
 * @since : 1.8
 */
public class ReplaceBenchmarkTest extends AbstractContainerBase {

    private static final boolean switcher = false;

    private static final boolean init = false;

    private LongIdGenerator idGenerator;

    private TransactionManager transactionManager = new DefaultTransactionManager(
            new IncreasingOrderLongIdGenerator(0));

    private SQLMasterStorage masterStorage;

    private SphinxQLIndexStorage indexStorage;

    private DataSourcePackage dataSourcePackage;

    private ExecutorService asyncThreadPool;

    private DevopsRebuildIndex taskExecutor;

    private int testLimits = 100000;

    private int submitLimit = 10;

    LocalDateTime now = LocalDateTime.now();

    private static long maintainId = 1;
    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING);
    private static IEntityField boolField = new EntityField(Long.MAX_VALUE - 2, "bool", FieldType.BOOLEAN);
    private static IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 3, "datetime", FieldType.DATETIME);
    private static IEntityField enumField = new EntityField(Long.MAX_VALUE - 5, "enum", FieldType.ENUM);
    private static IEntityField stringsField = new EntityField(Long.MAX_VALUE - 6, "strings", FieldType.STRINGS);

    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE - 1, "test",
            Arrays.asList(longField, stringField, boolField, dateTimeField, enumField, stringsField));

    @After
    public void after() {
        if (null != dataSourcePackage) {
            dataSourcePackage.close();
        }
    }

    @Before
    public void before() throws Exception {
        if (switcher) {
            if (null == dataSourcePackage) {
                dataSourcePackage = buildDataSource("./src/test/resources/sql_benchmark.conf");
            }

            asyncThreadPool = new ThreadPoolExecutor(submitLimit, submitLimit,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(100),
                    ExecutorHelper.buildNameThreadFactory("async-threads", false));

            idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));

            // 等待加载完毕
            TimeUnit.SECONDS.sleep(1L);

            //  初始化master
            masterStorage = initMaster(dataSourcePackage, transactionManager);

            //  初始化index
            indexStorage = initIndex(dataSourcePackage, transactionManager);

            // 等待加载完毕
            TimeUnit.SECONDS.sleep(1L);

            //  初始化task
            taskExecutor = initTaskExecutor(dataSourcePackage, masterStorage, indexStorage, idGenerator);

            if (init) {
                initData(testLimits * submitLimit);
            }
        }
    }

    @Test
    public void replaceTest() throws Exception {
        if (switcher) {
            TaskHandler taskHandler = taskExecutor.rebuildIndex(entityClass, now.minusDays(1), now);

            int i = 0;
            while (i < 20000) {
                if (taskHandler.isDone() ||
                        taskHandler.batchStatus().get() == BatchStatus.ERROR) {
                    break;
                }
                Thread.sleep(1000 * 2);
                i++;
            }

            Assert.assertEquals(100, taskHandler.getProgressPercentage());

            Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                    taskHandler.devOpsTaskInfo().getFinishSize());
        }
    }

    @Test
    public void testReplaceLimits() throws SQLException {
        if (switcher) {
            int finished = 0;
            for (int i = 0; i < testLimits; i++) {
                List<IEntity> entityList = new ArrayList<>();
                for (int j = 0; j < submitLimit; j++) {
                    finished++;
                    entityList.add(initEntity());
                }
                submit(entityList);
                System.out.println("finished " + finished);
            }
        }
    }

    @Test
    public void resumeTest() throws Exception {
        if (switcher) {
            TaskHandler taskHandler = taskExecutor.rebuildIndex(entityClass,
                    now.minusYears(1), now.plusSeconds(50));

        /*
            测试sleep N秒后暂停任务
         */
            Thread.sleep(10 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Thread.sleep(30 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Thread.sleep(60 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Thread.sleep(120 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Thread.sleep(300 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Thread.sleep(500 * 1000);

            cancelResumeByCondition(taskHandler.id());

            Optional<TaskHandler> task = null;
            while (true) {
                task = taskExecutor.syncTask(taskHandler.id());
                if (!task.isPresent()) {
                    break;
                }
                IDevOpsTaskInfo devOpsTaskInfo = task.get().devOpsTaskInfo();
                if (devOpsTaskInfo.getStatus() == BatchStatus.DONE.getCode() ||
                        devOpsTaskInfo.getStatus() == BatchStatus.ERROR.getCode()) {
                    break;
                }
                Thread.sleep(2000);
            }

            Assert.assertNotNull(task);
            Assert.assertTrue(task.isPresent());
            Assert.assertEquals(100, task.get().getProgressPercentage());
            Assert.assertEquals(task.get().devOpsTaskInfo().getBatchSize(), task.get().devOpsTaskInfo().getFinishSize());
        }
    }

    private void cancelResumeByCondition(String taskId) throws Exception {
        Optional<TaskHandler> task = taskExecutor.syncTask(taskId);
        if (task.isPresent()) {
            TaskHandler taskHandler = task.get();
            taskHandler.cancel();
            Thread.sleep(30 * 1000);

            taskHandler = taskExecutor.resumeIndex(entityClass, taskHandler.devOpsTaskInfo().id(), 0);

            Assert.assertNotNull(taskHandler.devOpsTaskInfo());
        }
    }


    private void submit(List<IEntity> entityList) throws SQLException {
        CountDownLatch countDownLatch = new CountDownLatch(entityList.size());
        List<Future> futures = new ArrayList<>(entityList.size());
        entityList.forEach(
                entity -> {
                    futures.add(asyncThreadPool.submit(new ReIndexCallable(countDownLatch, entity)));
                }
        );

        try {
            if (!countDownLatch.await(30 * 1000, TimeUnit.MILLISECONDS)) {
                for (Future f : futures) {
                    f.cancel(true);
                }
                throw new SQLException("reIndex failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
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
                int reIndex = indexStorage.replace(entity);
                if (NULL_UPDATE == reIndex) {
                    throw new SQLException("reIndex failed, no one replace.");
                }
                return reIndex;
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    // 初始化数据
    private void initData(int size) throws Exception {
        /*  测试任务失败继续 */
        List<IEntity> resumeEntities = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            IEntity entity = initEntity();

            resumeEntities.add(entity);
        }

        try {
            for (IEntity v : resumeEntities) {
                try {
                    masterStorage.build(v);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private IEntity initEntity() {
        long id = idGenerator.next();
        IEntityValue values = new EntityValue(id);
        String v = Long.toHexString(id);
        values.addValues(Arrays.asList(new LongValue(longField, id),
                new StringValue(stringField, "v1" + v),
                new BooleanValue(boolField, id % 2 == 0),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
                new EnumValue(enumField, "1"),
                new StringsValue(stringsField, "v" + 1, "v" + 2)));
        IEntity entity = new Entity(id, entityClass, values);
        entity.markTime(System.currentTimeMillis());
        entity.restMaintainId(maintainId);

        return entity;
    }
}
