package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 基于KV的任务队列.
 *
 * @author weikai
 * @version 1.0 2021/8/13 18:06
 * @since 1.8
 */
class TaskKeyValueQueueTest {
    private TaskKeyValueQueue instance;
    private KeyValueStorage keyValueStorage;
    private static final String NAME = "test";
    private ExecutorService worker;
    private Logger logger = LoggerFactory.getLogger(TaskKeyValueQueueTest.class);

    @BeforeEach
    void before() throws Exception {
        worker = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(10000),
                ExecutorHelper.buildNameThreadFactory("task", false),
                new ThreadPoolExecutor.AbortPolicy()
        );

        instance = new TaskKeyValueQueue(NAME);
        keyValueStorage = new MemoryKeyValueStorage();
        Field kvField = TaskKeyValueQueue.class.getDeclaredField("kv");
        kvField.setAccessible(true);
        kvField.set(instance, keyValueStorage);

        Field locker = TaskKeyValueQueue.class.getDeclaredField("locker");
        locker.setAccessible(true);
        locker.set(instance, new LocalResourceLocker());

        Field idGenerator = TaskKeyValueQueue.class.getDeclaredField("idGenerator");
        idGenerator.setAccessible(true);
        idGenerator.set(instance, new MockIdGenerator());

        Field serializeStrategy = TaskKeyValueQueue.class.getDeclaredField("serializeStrategy");
        serializeStrategy.setAccessible(true);
        serializeStrategy.set(instance, new HessianSerializeStrategy());

        instance.init();
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        instance.destroy();
        keyValueStorage = null;
        instance = null;
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    /**
     * 测试添加单个任务.
     *
     * @throws NoSuchFieldException .
     * @throws IllegalAccessException .
     */
    @Test
    public void testAppendOne() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        instance.append(new MockTask());
        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap dataMap = (ConcurrentMap) data.get(keyValueStorage);

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        TimeUnit.SECONDS.sleep(5);
        Assertions.assertTrue(dataMap.containsKey(prefix + "-" + 1));
        Assertions.assertEquals(dataMap.size(), 1);
    }

    /**
     * 测试添加空任务，任务队列应该为空.
     *
     * @throws NoSuchFieldException .
     * @throws IllegalAccessException .
     */
    @Test
    public void testAppendNull() throws NoSuchFieldException, IllegalAccessException {
        instance.append(null);
        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap dataMap = (ConcurrentMap) data.get(keyValueStorage);

        Assertions.assertEquals(dataMap.size(), 0);
    }

    /**
     * 并发添加任务，断言任务队列elementKey是否符合预期，断言任务数.
     *
     * @throws InterruptedException .
     * @throws IllegalAccessException .
     * @throws NoSuchFieldException .
     */
    @Test
    public void testAppend() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        int count = 10000;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new MockTask());
                    latch.countDown();
                }
            });
        }
        latch.await();

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) data.get(keyValueStorage);

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < count; i++) {
            Assertions.assertTrue(map.containsKey(prefix + "-" + (i + 1)));
        }
        Assertions.assertEquals(map.size(), count);
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
                    instance.append(new MockTask());
                }
            });
        }
        latch.await();

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap dataMap = (ConcurrentMap) data.get(keyValueStorage);
        Assertions.assertEquals(dataMap.size(), count);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTaskSize");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);


        Assertions.assertEquals(longMap.get(point).get(), count);
        Assertions.assertEquals(longMap.get(unused).get(), 0);
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
                    instance.append(new MockTask());
                }
            });
        }
        latch.await();

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap dataMap = (ConcurrentMap) data.get(keyValueStorage);
        Assertions.assertEquals(dataMap.size(), count);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTaskSize");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);


        Assertions.assertEquals(longMap.get(point).get(), count);
        Assertions.assertEquals(longMap.get(unused).get(), 0);
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
                    instance.append(new MockTask());
                }
            });
        }

        latch.await();

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap dataMap = (ConcurrentMap) data.get(keyValueStorage);
        Assertions.assertEquals(dataMap.size(), 0);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTaskSize");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);

        Assertions.assertEquals(longMap.get(point).get(), count);
        Assertions.assertEquals(longMap.get(unused).get(), 0);
    }

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

    public static class MockIdGenerator implements LongIdGenerator {
        private AtomicLong id = new AtomicLong(0);
        private ConcurrentMap<String, AtomicLong> atomicLongConcurrentMap = new ConcurrentHashMap<>();
        private ReentrantLock lock = new ReentrantLock();

        @Override
        public Long next() {
            return id.addAndGet(1L);
        }

        @Override
        public Long next(String nameSpace) {
            AtomicLong atomicLong;
            try {
                lock.lock();
                if (!atomicLongConcurrentMap.containsKey(nameSpace)) {
                    atomicLongConcurrentMap.put(nameSpace, new AtomicLong(0));
                } else {
                    atomicLongConcurrentMap.get(nameSpace);
                }
            } finally {
                lock.unlock();
            }
            atomicLong = atomicLongConcurrentMap.get(nameSpace);
            return atomicLong.addAndGet(1L);
        }

        @Override
        public boolean supportNameSpace() {
            return true;
        }

        @Override
        public boolean isContinuous() {
            return true;
        }

        @Override
        public boolean isPartialOrder() {
            return true;
        }

        @Override
        public void reset() {

        }

        @Override
        public void reset(String ns) {
            LongIdGenerator.super.reset(ns);
        }
    }
}