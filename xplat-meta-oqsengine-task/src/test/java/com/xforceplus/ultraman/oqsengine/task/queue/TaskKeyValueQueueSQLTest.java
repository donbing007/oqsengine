package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.id.RedisOrderContinuousLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.SqlKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction.SqlKvConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.mock.KVDbScript;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * TaskQueue磁盘测试.
 *
 * @author weikai
 * @version 1.0 2021/8/20 13:43
 * @since 1.8
 */
@ExtendWith({MysqlContainer.class, RedisContainer.class})
public class TaskKeyValueQueueSQLTest {
    private TaskKeyValueQueue instance;
    private static final String NAME = "test";
    private ExecutorService worker;
    private Logger logger = LoggerFactory.getLogger(TaskKeyValueQueueTest.class);
    private SqlKeyValueStorage keyValueStorage;
    private DataSource ds;
    private RedisClient redisClient;
    private RedisOrderContinuousLongIdGenerator redisOrderContinuousLongIdGenerator;

    /**
     * 初始化测试实例.
     */
    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:oqsengine-ds.conf");

        worker = new ThreadPoolExecutor(5, 5,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue(10000),
            ExecutorHelper.buildNameThreadFactory("task", false),
            new ThreadPoolExecutor.AbortPolicy()
        );

        instance = new TaskKeyValueQueue(NAME);

        keyValueStorage = new SqlKeyValueStorage();
        keyValueStorage.setTableName("kv");
        keyValueStorage.setTimeoutMs(200);
        ds = CommonInitialization.getInstance().getDataSourcePackage(true).getFirstMaster();

        try (Connection conn = ds.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute(KVDbScript.DROP_KV);
                st.execute(KVDbScript.CREATE_KV);
            }
        }

        AutoJoinTransactionExecutor executor = new AutoJoinTransactionExecutor(
                StorageInitialization.getInstance().getTransactionManager(),
                new SqlKvConnectionTransactionResourceFactory(),
                NoSelector.build(ds),
                NoSelector.build("kv"));

        Collection<Field> fields = ReflectionUtils.printAllMembers(keyValueStorage);
        ReflectionUtils.reflectionFieldValue(fields, "transactionExecutor", keyValueStorage, executor);

        ResourceLocker locker = new LocalResourceLocker();
        fields = ReflectionUtils.printAllMembers(keyValueStorage);
        ReflectionUtils.reflectionFieldValue(fields, "locker", keyValueStorage, locker);

        Field kvField = TaskKeyValueQueue.class.getDeclaredField("kv");
        kvField.setAccessible(true);
        kvField.set(instance, keyValueStorage);

        Field lockerField = TaskKeyValueQueue.class.getDeclaredField("locker");
        lockerField.setAccessible(true);
        lockerField.set(instance, new LocalResourceLocker());

        Field idGenerator = TaskKeyValueQueue.class.getDeclaredField("idGenerator");
        idGenerator.setAccessible(true);

        redisClient = CommonInitialization.getInstance().getRedisClient();
        redisOrderContinuousLongIdGenerator = new RedisOrderContinuousLongIdGenerator(redisClient, NAME);
        redisOrderContinuousLongIdGenerator.init();
        idGenerator.set(instance, redisOrderContinuousLongIdGenerator);

        Field serializeStrategy = TaskKeyValueQueue.class.getDeclaredField("serializeStrategy");
        serializeStrategy.setAccessible(true);
        serializeStrategy.set(instance, new HessianSerializeStrategy());
        instance.init();
    }

    /**
     * 每次测试后的清理.
     */
    @AfterEach
    public void after() throws Exception {
        instance.destroy();
        try (Connection conn = ds.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("truncate table kv");
            }
        }
        redisOrderContinuousLongIdGenerator.reset();
        ds = null;
        instance.destroy();
        keyValueStorage = null;
        instance = null;
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    /**
     * 测试添加单个任务.
     *
     * @throws NoSuchFieldException   .
     * @throws IllegalAccessException .
     */
    @Test
    public void testAppendOne() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        instance.append(new MockTask());

        Field unSubmitTask = instance.getClass().getDeclaredField("unSubmitTask");
        unSubmitTask.setAccessible(true);
        ConcurrentHashMap<String, byte[]> unSubmitTaskMap = (ConcurrentHashMap<String, byte[]>) unSubmitTask.get(instance);

        long millis = System.currentTimeMillis();
        while (!unSubmitTaskMap.isEmpty()) {
            if (System.currentTimeMillis() - millis > 180000) {
                break;
            }
        }

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        Optional<byte[]> bytes = keyValueStorage.get(prefix + "-" + initPoint);
        Task task = new HessianSerializeStrategy().unserialize(bytes.get(), Task.class);
        Assertions.assertEquals(task.location(), initPoint);
        Assertions.assertTrue(keyValueStorage.exist(prefix + "-" + initPoint));

        Assertions.assertEquals(keyValueStorage.incr(NAME + "-unused", 0), 1);
    }

    /**
     * 测试添加空任务，任务队列应该为空.
     *
     * @throws NoSuchFieldException   .
     * @throws IllegalAccessException .
     */
    @Test
    public void testAppendNull() throws NoSuchFieldException, IllegalAccessException {
        instance.append(null);

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        Assertions.assertFalse(keyValueStorage.exist(prefix + "-" + initPoint));
    }

    /**
     * 并发添加任务，断言任务队列elementKey是否符合预期，断言任务数.
     *
     * @throws InterruptedException   .
     * @throws IllegalAccessException .
     * @throws NoSuchFieldException   .
     */
    @Test
    public void testAppend() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        long start;
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new TaskKeyValueQueueTest.MockTask());
                    latch.countDown();
                }
            });
        }
        latch.await();

        Field unSubmitTask = instance.getClass().getDeclaredField("unSubmitTask");
        unSubmitTask.setAccessible(true);
        ConcurrentHashMap<String, byte[]> unSubmitTaskMap = (ConcurrentHashMap<String, byte[]>) unSubmitTask.get(instance);

        long millis = System.currentTimeMillis();
        while (!unSubmitTaskMap.isEmpty()) {
            if (System.currentTimeMillis() - millis > 180000) {
                break;
            }
        }

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        for (long i = initPoint; i <= count; i++) {
            if (!keyValueStorage.exist(prefix + "-" + i)) {
                logger.info(prefix + "-" +i);
            }
            Assertions.assertTrue(keyValueStorage.exist(prefix + "-" + i));
        }
        Assertions.assertEquals(keyValueStorage.incr(NAME + "-unused", 0), count);

        logger.info("start time is : {}", start);
    }

    /**
     * 并发添加任务，并发取出任务（没有任务阻塞），pointKey指向位置断言.
     *
     * @throws Exception .
     */
    @Test
    public void testGet() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < 3; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Task task = instance.get();
                        if (task != null) {
                            try {
                                lock.lock();
                                latch.countDown();
                                if (latch.getCount() <= 2) {
                                    break;
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                }
            });
        }

        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new TaskKeyValueQueueTest.MockTask());
                }
            });
        }
        latch.await();

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        Field pointKeyField = instance.getClass().getDeclaredField("pointKey");
        pointKeyField.setAccessible(true);
        String pointKey = (String) pointKeyField.get(instance);


        for (long i = initPoint; i <= count; i++) {
            Assertions.assertTrue(keyValueStorage.exist(prefix + "-" + i));
        }
        Assertions.assertEquals(keyValueStorage.incr(NAME + "-unused", 0), 0);
        Assertions.assertEquals(keyValueStorage.incr(pointKey, 0), count);

    }

    /**
     * 并发添加任务，并发取出任务（阻塞指定时长），pointKey指向位置断言.
     *
     * @throws Exception .
     */
    @Test
    public void testGetWithTimeOut() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < 3; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Task task = instance.get(1000L);
                        if (task != null) {
                            try {
                                lock.lock();
                                latch.countDown();
                                if (latch.getCount() <= 2) {
                                    break;
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                }
            });
        }

        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new TaskKeyValueQueueTest.MockTask());
                }
            });
        }
        latch.await();

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        Field pointKeyField = instance.getClass().getDeclaredField("pointKey");
        pointKeyField.setAccessible(true);
        String pointKey = (String) pointKeyField.get(instance);


        for (long i = initPoint; i <= count; i++) {
            Assertions.assertTrue(keyValueStorage.exist(prefix + "-" + i));
        }
        Assertions.assertEquals(keyValueStorage.incr(NAME + "-unused", 0), 0);
        Assertions.assertEquals(keyValueStorage.incr(pointKey, 0), count);

        instance.destroy();
    }

    /**
     * 并发添加任务，并发取出任务（没有任务阻塞），取出任务后队列为空，pointKey指向位置断言.
     *
     * @throws Exception .
     */
    @Test
    public void testAck() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < 3; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Task task = instance.get();
                        if (task != null) {
                            instance.ack(task);
                            try {
                                lock.lock();
                                latch.countDown();
                                logger.info("latch = " + latch.getCount());
                                if (latch.getCount() <= 2) {
                                    break;
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                }
            });
        }

        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new TaskKeyValueQueueTest.MockTask());
                }
            });
        }

        latch.await();
        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        Field pointKeyField = instance.getClass().getDeclaredField("pointKey");
        pointKeyField.setAccessible(true);
        String pointKey = (String) pointKeyField.get(instance);


        for (long i = initPoint; i <= count; i++) {
            Assertions.assertFalse(keyValueStorage.exist(prefix + "-" + i));
        }
        Assertions.assertEquals(keyValueStorage.incr(NAME + "-unused", 0), 0);
        Assertions.assertEquals(keyValueStorage.incr(pointKey, 0), count);

    }

    /**
     * 模拟内存丢失，断言丢失任务数在一百以内.
     *
     * @throws InterruptedException      .
     * @throws NoSuchFieldException      .
     * @throws IllegalAccessException    .
     * @throws NoSuchMethodException     .
     * @throws InvocationTargetException .
     */
    @Test
    public void testShutdown() throws InterruptedException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        int count = 1000;
        AtomicLong appendCount = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new TaskKeyValueQueueTest.MockTask());
                    appendCount.addAndGet(1);
                    latch.countDown();
                }
            });
        }


        Method destroyNow = instance.getClass().getDeclaredMethod("destroyNow");
        destroyNow.setAccessible(true);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(600L));
        destroyNow.invoke(instance);

        while (latch.getCount() != 0) {
            latch.countDown();
        }
        latch.await();

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        Field initPointField = instance.getClass().getDeclaredField("initPoint");
        initPointField.setAccessible(true);
        long initPoint = (long) initPointField.get(instance);

        long lostSize = 0;
        long hasSave = 0;
        for (long i = initPoint; i <= count; i++) {
            hasSave ++;
            if (!keyValueStorage.exist(prefix + "-" + i)) {
                lostSize = appendCount.get() - i;
                break;
            }
        }
        Assertions.assertTrue(lostSize <= count );

    }

    /**
     * mockTask.
     */
    public static class MockTask implements Task, Serializable {
        private static final long serialVersionUID = 1L;
        private long location;

        @Override
        public String id() {
            return null;
        }

        @Override
        public long location() {
            return location;
        }

        @Override
        public void setLocation(long l) {
            this.location = l;
        }

        @Override
        public long createTime() {
            return 0;
        }

        @Override
        public Class runnerType() {
            return null;
        }
    }

}
