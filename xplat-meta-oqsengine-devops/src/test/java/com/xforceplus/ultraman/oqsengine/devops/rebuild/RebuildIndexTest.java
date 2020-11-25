package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.devops.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.handler.TaskHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * desc :
 * name : RebuildIndexTest
 *
 * @author : xujia
 * date : 2020/8/27
 * @since : 1.8
 */
public class RebuildIndexTest extends AbstractContainer {
    final Logger logger = LoggerFactory.getLogger(RebuildIndexTest.class);

    private RebuildIndexExecutor rebuildIndexExecutor;

    private int pageNo = 1;
    private int pageSize = 10;

    private int doneTask = 0;

    LocalDateTime now = LocalDateTime.now();
    private long timeId = now.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

    IEntity[] entities;
    List<Long> expectedList = new ArrayList<>();
    List<Long> surplusList = new ArrayList<>();

    private Collection<IEntityField> expectEntityFields;
    private Collection<IEntityField> surplusEntityFields;
    private Collection<IEntityField> fEntityFields;
    private Collection<IEntityField> cEntityFields;

    private Collection<IEntityField> resumeEntityFields;
    private IEntityClass resumeEntityClass;

    private IEntityClass fatherEntityClass;
    private IEntityClass childEntityClass;
    private IEntityClass surplusClass;

    private IEntityClass expectEntityClass;

    private int testVersion = 0;

    private int testResumeCount = 3000;

    private void initEntityClass() throws SQLException {
         /*
            simple test use
         */
        expectEntityFields = Arrays.asList(
                new EntityField(10001, "e1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(10002, "e2", FieldType.STRING, FieldConfig.build().searchable(true))
        );

        expectEntityClass =
                new EntityClass(10001L,  "test", null,
                        null, null, expectEntityFields);

         /*
            surplus test use
         */
        surplusEntityFields = Arrays.asList(
                new EntityField(20001, "s1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(20002, "s2", FieldType.STRING, FieldConfig.build().searchable(true))
        );

        surplusClass =
                new EntityClass(20001, "sur", null,
                        null, null, surplusEntityFields);

         /*
            pref/cref test use
         */
        fEntityFields = Arrays.asList(
                new EntityField(30001, "f1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(30002, "f2", FieldType.STRING, FieldConfig.build().searchable(true))
        );

        cEntityFields = Arrays.asList(
                new EntityField(40001, "c1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(40002, "c2", FieldType.STRING, FieldConfig.build().searchable(true))
        );

        fatherEntityClass = new EntityClass(30001, "father", null,
                null, null, fEntityFields);


        childEntityClass = new EntityClass(
                40001, "chlid", null,
                null, fatherEntityClass, cEntityFields);

        /*
            resume test use
         */
        resumeEntityFields = Arrays.asList(
                new EntityField(50001, "r1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new EntityField(50002, "r2", FieldType.STRING, FieldConfig.build().searchable(true))
        );
        resumeEntityClass = new EntityClass(50001, "resume", null,
                null, null, resumeEntityFields);
    }

    @Before
    public void before() throws Exception {

        start();

        initEntityClass();

        truncate();

        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        //  主库插入一条, 索引库写入一条
        initData(100);

        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());
    }

    @After
    public void after() throws SQLException {
        clear();
        close();
    }


    @Test
    public void rebuildIndexSimple() throws Exception {
        TaskHandler taskHandler = taskExecutor.rebuildIndex(expectEntityClass,
                now,
                now.plusSeconds(5));

        int i = 0;
        while (i < 50) {
            if (taskHandler.isDone()) {
                break;
            }
            Thread.sleep(1000 * 3);
            i++;
        }

        Assert.assertEquals(100, taskHandler.getProgressPercentage());

        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        doneTask++;
    }

    @Test
    public void rebuildIndexDeleteSurPlus() throws Exception {
        TaskHandler taskHandler = taskExecutor.rebuildIndex(surplusClass,
                now,
                now.plusSeconds(5));

        int i = 0;
        while (i < 50) {
            if (taskHandler.isDone()) {
                break;
            }
            Thread.sleep(1000 * 3);
            i++;
        }

        Assert.assertEquals(100, taskHandler.getProgressPercentage());

        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        doneTask++;
    }

    @Test
    public void rebuildIndexIncludePrefCref() throws Exception {
        TaskHandler taskHandler = taskExecutor.rebuildIndex(childEntityClass,
                now, now.plusSeconds(5));

        int i = 0;
        while (i < 50) {
            if (taskHandler.isDone()) {
                break;
            }
            Thread.sleep(1000 * 3);
            i++;
        }

        Assert.assertEquals(100, taskHandler.getProgressPercentage());

        Assert.assertEquals(taskHandler.devOpsTaskInfo().getBatchSize(),
                taskHandler.devOpsTaskInfo().getFinishSize());

        Assert.assertTrue(taskHandler.isDone());

        doneTask++;
    }

    @Test
    public void rebuildIndexWithMonitorStatus() throws Exception {
        TaskHandler taskHandler = taskExecutor.rebuildIndex(childEntityClass,
                now, now.plusSeconds(5));

        int i = 0;
        while (i < 300) {
            if (taskHandler.isDone()) {
                break;
            }

            logger.debug("ProgressPercentage : {}", taskHandler.getProgressPercentage());
            Thread.sleep(500);
            i++;
        }
        logger.debug("ProgressPercentage : {}, isDone : {}", taskHandler.getProgressPercentage(), taskHandler.isDone());
        if (taskHandler.isDone()) {
            doneTask++;
        }
    }

    @Test
    public void resumeTest() throws Exception {
        TaskHandler taskHandler = taskExecutor.rebuildIndex(resumeEntityClass,
                now.minusSeconds(50), now.plusSeconds(50));

        /*
            测试sleep N秒后暂停任务
         */
        int tries = 0;
        int max = 50;
        while (true) {
            if (taskHandler.devOpsTaskInfo().getFinishSize() > 0 &&
                    taskHandler.devOpsTaskInfo().getFinishSize() < testResumeCount) {
                cancelResumeByCondition(taskHandler.id());
                break;
            }

            if (tries >= max) {
                break;
            }
            Thread.sleep(100);
            tries ++;
        }

        Assert.assertTrue(tries < max);

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

        doneTask++;
    }

    private void cancelResumeByCondition(String taskId) throws Exception {
        Optional<TaskHandler> task = taskExecutor.syncTask(taskId);
        if (task.isPresent()) {
            TaskHandler taskHandler = task.get();
            taskHandler.cancel();
            Thread.sleep(45 * 1000);

            taskHandler = taskExecutor.resumeIndex(resumeEntityClass, taskHandler.devOpsTaskInfo().id(), 0);

            Assert.assertNotNull(taskHandler.devOpsTaskInfo());
        }
    }

    public void listAllTasks() throws Exception {
        Thread.sleep(1000);
        Page page = new Page(pageNo, pageSize);
        Collection<TaskHandler> taskHandlers = taskExecutor.listAllTasks(page);
        Assert.assertEquals(doneTask, taskHandlers.size());
        for (TaskHandler taskHandler : taskHandlers) {
            Assert.assertTrue(taskHandler.isDone());
        }
    }

    /*

        data init

     */
    private void initExpected(int size) throws SQLException {
        /*  测试1000条索引记录重建 */
        List<IEntity> expectedEntitiesWithTime = new ArrayList<>(size);

        for (int i = 1; i < size; i++) {
            long entityId = timeId + i;
            expectedList.add(entityId);
            expectedEntitiesWithTime.add(buildEntityWithEntityClassInit(10001, entityId, timeId,
                    new EntityFamily(0, 0), expectEntityFields));
        }

        try {
            //  正常测试1000条索引重建
            for (IEntity v : expectedEntitiesWithTime) {
                try {
                    masterStorage.build(v);
                    indexStorage.build(v);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            transactionManager.getCurrent().get().rollback();
            throw e;
        }
    }

    private void initSurplus(int size) throws SQLException {
        /*  测试重建索引时包含有多余的记录 */
        List<IEntity> surplusEntityList = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            long entityId = timeId * 2 + i;
            surplusList.add(entityId);
            surplusEntityList.add(buildEntityWithEntityClassInit(20001, entityId, timeId,
                    new EntityFamily(0, 0), surplusEntityFields));
        }

        try {
            int i = 0;
            for (IEntity v : surplusEntityList) {
                try {
                    i++;
                    if (i < (surplusEntityList.size() * 2) / 3) {
                        masterStorage.build(v);
                    }

                    indexStorage.build(v);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            transactionManager.getCurrent().get().rollback();
            throw e;
        }
    }

    private void initPref(int size) throws SQLException {
        /*  测试子类包含父类需要重建 */
        List<AbstractMap.SimpleEntry<IEntity, IEntity>> refEntityList = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            long entityId = i;

            IEntity fEntity = new Entity(timeId * 3 + entityId, fatherEntityClass,
                    buildRandomValue(timeId * 3 + entityId, fEntityFields), new EntityFamily(0, 0), testVersion);
            fEntity.markTime(timeId);

            IEntity cEntity = new Entity(timeId * 3 + entityId + 10000, childEntityClass,
                    buildRandomValue(timeId * 3 + entityId + 10000, cEntityFields),
                    new EntityFamily(timeId * 3 + entityId, 0), testVersion);
            cEntity.markTime(timeId);

            refEntityList.add(new AbstractMap.SimpleEntry<>(fEntity, cEntity));
        }
        try {
            int i = 0;
            for (AbstractMap.SimpleEntry<IEntity, IEntity> v : refEntityList) {
                try {
                    i++;
                    if (i < (refEntityList.size() * 3) / 5) {
                        masterStorage.build(v.getKey());
                        masterStorage.build(v.getValue());
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            transactionManager.getCurrent().get().rollback();
            throw e;
        }
    }

    private void initResume(int size) throws SQLException {
        /*  测试任务失败继续 */
        List<IEntity> resumeEntities = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            long entityId = size + i;

            IEntity rEntity = new Entity(timeId * 4 + entityId, resumeEntityClass,
                    buildRandomValue(timeId * 4 + entityId, resumeEntityFields), new EntityFamily(0, 0), testVersion);
            rEntity.markTime(timeId);

            resumeEntities.add(rEntity);
        }

        try {
            int i = 0;
            for (IEntity v : resumeEntities) {
                try {
                    i++;
                    masterStorage.build(v);
                    indexStorage.build(v);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            transactionManager.getCurrent().get().rollback();
            throw e;
        }
    }

    // 初始化数据
    private void initData(int size) throws Exception {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            initExpected(size);

            initSurplus(size / 100);

            initPref(size);

            initResume(testResumeCount);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            transactionManager.finish(tx);
        }
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    private IEntity buildEntityWithEntityClassInit(long entityClassId, long id, long time, IEntityFamily family, Collection<IEntityField> fields) {
        IEntity entity = new Entity(
                id,
                new EntityClass(entityClassId, "test", fields),
                buildRandomValue(id, fields),
                family,
                testVersion
        );
        entity.markTime(time);
        return entity;
    }


    private void truncate() throws SQLException {

        if (entities != null) {
            for (IEntity entity : entities) {
                indexStorage.delete(entity);
            }

            for (IEntity entity : entities) {
                masterStorage.delete(entity);
            }
        }
    }
}
