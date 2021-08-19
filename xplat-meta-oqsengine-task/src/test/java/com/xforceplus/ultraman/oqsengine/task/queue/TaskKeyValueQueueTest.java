package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        worker = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(10000),
                ExecutorHelper.buildNameThreadFactory("task", false),
                new ThreadPoolExecutor.AbortPolicy()
        );

        instance = new TaskKeyValueQueue(NAME);
        keyValueStorage = new MemoryKeyValueStorage();
        Field taskField = TaskKeyValueQueue.class.getDeclaredField("kv");
        taskField.setAccessible(true);
        taskField.set(instance, keyValueStorage);

        Field locker = TaskKeyValueQueue.class.getDeclaredField("locker");
        locker.setAccessible(true);
        locker.set(instance, new LocalResourceLocker());

        Field idGenerator = TaskKeyValueQueue.class.getDeclaredField("idGenerator");
        idGenerator.setAccessible(true);
        idGenerator.set(instance, new MockIdGenerator());

        Field serializeStrategy = TaskKeyValueQueue.class.getDeclaredField("serializeStrategy");
        serializeStrategy.setAccessible(true);
        serializeStrategy.set(instance, new HessianSerializeStrategy());
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        keyValueStorage = null;
        instance = null;
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    @Test
    void append() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
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
        Field kv = instance.getClass().getDeclaredField("kv");
        kv.setAccessible(true);
        KeyValueStorage keyValueStorage = (KeyValueStorage) kv.get(instance);
        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) data.get(keyValueStorage);

        Field elementKeyPrefix = instance.getClass().getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        String prefix = (String) elementKeyPrefix.get(instance);

        for (int i = 0; i < count; i++) {
            Assertions.assertTrue(map.containsKey(prefix + "-" + (i + 1)));
        }
        Assertions.assertEquals(map.size(), count);

    }

    @Test
    void get() throws Exception {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        worker.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    instance.get();
                    logger.info("---get task ---");
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        break;
                    }
                }
            }
        });
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new MockTask());
                }
            });
        }
        latch.await();
        Field kv = instance.getClass().getDeclaredField("kv");
        kv.setAccessible(true);
        KeyValueStorage keyValueStorage = (KeyValueStorage) kv.get(instance);

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) data.get(keyValueStorage);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTask");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);

        Assertions.assertEquals(map.size(), count);
        Assertions.assertEquals(longMap.get(point).get(), count + 1);
        Assertions.assertEquals(longMap.get(unused).get(), 0);
    }

    @Test
    void testGet() throws Exception {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        worker.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    instance.get(1000L);
                    logger.info("---get task ---");
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        break;
                    }
                }
            }
        });
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new MockTask());
                }
            });
        }
        latch.await();
        Field kv = instance.getClass().getDeclaredField("kv");
        kv.setAccessible(true);
        KeyValueStorage keyValueStorage = (KeyValueStorage) kv.get(instance);

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) data.get(keyValueStorage);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTask");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);

        Assertions.assertEquals(map.size(), count);
        Assertions.assertEquals(longMap.get(point).get(), count + 1);
        Assertions.assertEquals(longMap.get(unused).get(), 0);
    }

    @Test
    void ack() throws Exception {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        worker.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Task task = instance.get();
                    instance.ack(task);
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        break;
                    }
                }
            }
        });
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    instance.append(new MockTask());
                }
            });
        }

        latch.await();
        Field kv = instance.getClass().getDeclaredField("kv");
        kv.setAccessible(true);
        KeyValueStorage keyValueStorage = (KeyValueStorage) kv.get(instance);

        Field data = MemoryKeyValueStorage.class.getDeclaredField("data");
        data.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) data.get(keyValueStorage);

        Field numberData = MemoryKeyValueStorage.class.getDeclaredField("numberData");
        numberData.setAccessible(true);
        ConcurrentMap<String, AtomicLong> longMap = (ConcurrentMap) numberData.get(keyValueStorage);

        Field pointKey = TaskKeyValueQueue.class.getDeclaredField("pointKey");
        pointKey.setAccessible(true);
        String point = (String) pointKey.get(instance);

        Field unusedTask = instance.getClass().getDeclaredField("unusedTask");
        unusedTask.setAccessible(true);
        String unused = (String) unusedTask.get(instance);

        Assertions.assertEquals(map.size(), 0);
        Assertions.assertEquals(longMap.get(point).get(), count + 1);
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
    }
}