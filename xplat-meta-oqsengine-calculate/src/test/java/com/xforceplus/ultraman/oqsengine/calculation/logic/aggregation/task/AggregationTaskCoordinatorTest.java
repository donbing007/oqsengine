package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.MetaParseTree;
import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.HessianSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskKeyValueQueue;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.set.ListOrderedSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 聚合初始化任务协调者.
 *
 * @author weikai
 * @version 1.0 2021/9/7 10:30
 * @since 1.8
 */
class AggregationTaskCoordinatorTest {
    private AggregationTaskCoordinator aggregationTaskCoordinator;
    private KeyValueStorage kv;
    private SerializeStrategy serializeStrategy;
    private ResourceLocker locker;
    private ConcurrentHashMap<String, TaskQueue> taskQueueMap;
    private ConcurrentHashMap<String, TaskQueue> usingApp;
    private ExecutorService worker;
    private ConcurrentMap<String, TaskRunner> runners;
    private List<String> finishedTaskIds;
    private static final String TEST0 = "test-0";
    private static final String TEST1 = "test-1";


    @BeforeEach
    public void before() throws Exception {
        worker = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(10000),
                ExecutorHelper.buildNameThreadFactory("task", false),
                new ThreadPoolExecutor.AbortPolicy()
        );
        aggregationTaskCoordinator = new AggregationTaskCoordinator();
        aggregationTaskCoordinator.setWorker(worker);


        Field kvField = aggregationTaskCoordinator.getClass().getDeclaredField("kv");
        kvField.setAccessible(true);
        kv = new MockMemoryKV();
        kvField.set(aggregationTaskCoordinator, kv);

        locker = new LocalResourceLocker();
        Field lockerField = aggregationTaskCoordinator.getClass().getDeclaredField("locker");
        lockerField.setAccessible(true);
        lockerField.set(aggregationTaskCoordinator, locker);

        Field serializeStrategyField = aggregationTaskCoordinator.getClass().getDeclaredField("serializeStrategy");
        serializeStrategyField.setAccessible(true);
        serializeStrategy = new HessianSerializeStrategy();
        serializeStrategyField.set(aggregationTaskCoordinator, serializeStrategy);

        taskQueueMap = new ConcurrentHashMap<>();
        taskQueueMap.put(TEST0, new MockTaskQueue(TEST0));
        taskQueueMap.put(TEST1, new MockTaskQueue(TEST1));
        Field taskQueueMapField = aggregationTaskCoordinator.getClass().getDeclaredField("taskQueueMap");
        taskQueueMapField.setAccessible(true);
        taskQueueMapField.set(aggregationTaskCoordinator, taskQueueMap);

        usingApp = new ConcurrentHashMap<>();
        Field usingAppField = aggregationTaskCoordinator.getClass().getDeclaredField("usingApp");
        usingAppField.setAccessible(true);
        usingAppField.set(aggregationTaskCoordinator, usingApp);

        runners = new ConcurrentHashMap<>();
        Field runnersField = aggregationTaskCoordinator.getClass().getDeclaredField("runners");
        runnersField.setAccessible(true);
        runnersField.set(aggregationTaskCoordinator, runners);

        finishedTaskIds = new CopyOnWriteArrayList();
    }

    @AfterEach
    public void destroy() {
        aggregationTaskCoordinator.destroy();
        worker.shutdown();
        aggregationTaskCoordinator = null;
        kv = null;
        serializeStrategy = null;
        locker = null;
        taskQueueMap = null;
        usingApp = null;
        runners = null;
    }


    /**
     * 模拟多个oqs节点添加同一个任务，只会有一个节点添加，kv中只存一份保证不重复.
     */
    @Test
    public void testAddInitInfo() throws InterruptedException {
        List<ParseTree> list = new ArrayList<>();
        list.add(new MetaParseTree());
        int count = 5;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    aggregationTaskCoordinator.addInitAppInfo(TEST0, list);
                    latch.countDown();
                }
            });
        }
        latch.await();
        MockTaskQueue queue = (MockTaskQueue) taskQueueMap.get(TEST0);
        Assertions.assertTrue(queue.getTasks().size() == 1);
    }


    /**
     * 测试每个版本的全量树存储在磁盘中，防止意外丢失.
     */
    @Test
    public void testAddFullTree() {
        ArrayList<ParseTree> list = new ArrayList<>();
        list.add(new MetaParseTree());
        aggregationTaskCoordinator.addFullTree(TEST0, list);
        Assertions.assertTrue(kv.exist(TEST0));
    }


    @Test
    public void registerRunner() {
        MockTaskRunner runner = new MockTaskRunner(finishedTaskIds);
        aggregationTaskCoordinator.registerRunner(runner);


        Assert.assertEquals(1, aggregationTaskCoordinator.getRunners().size());
        Assert.assertTrue(aggregationTaskCoordinator.getRunners().containsKey(MockTaskRunner.class.getSimpleName()));

        aggregationTaskCoordinator.registerRunner(new MockTaskRunner(finishedTaskIds));
        Assert.assertTrue(aggregationTaskCoordinator.getRunners().containsValue(runner));
    }


    /**
     * 测试并发添加初始化应用.
     *
     * @throws Exception 异常
     */
    @Test
    public void testAddOrderInfo() throws Exception {
        Method addOrderInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("addOrderInfo", String.class);
        addOrderInfo.setAccessible(true);
        int count = 5;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            int finalI = i;
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        addOrderInfo.invoke(aggregationTaskCoordinator, TEST0 + "-" + finalI);
                        latch.countDown();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        latch.await();

        Optional<byte[]> bytes = kv.get(AggregationTaskCoordinator.APP_INIT_ORDER);
        String byteToString = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
        for (int i = 0; i < count; i++) {
            Assertions.assertTrue(byteToString.contains(TEST0 + "-" + i));
        }

    }


    /**
     * 测试获取当前队列第一个聚合初始化应用.
     *
     * @throws Exception 异常
     */
    @Test
    public void testGetProcessingAppInfo() throws Exception {
        Method addOrderInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("addOrderInfo", String.class);
        addOrderInfo.setAccessible(true);
        addOrderInfo.invoke(aggregationTaskCoordinator, TEST0);
        Method getProcessingAppInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("getProcessingAppInfo");
        getProcessingAppInfo.setAccessible(true);
        Object invoke = getProcessingAppInfo.invoke(aggregationTaskCoordinator);
        Assertions.assertTrue(invoke.toString().equals(TEST0));
    }


    /**
     * 测试应用初始化队列移除指定应用.
     *
     * @throws Exception 异常
     */
    @Test
    public void testRemoveAppInfoFromOrderList() throws Exception {
        Method addOrderInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("addOrderInfo", String.class);
        addOrderInfo.setAccessible(true);
        addOrderInfo.invoke(aggregationTaskCoordinator, TEST0);
        addOrderInfo.invoke(aggregationTaskCoordinator, TEST1);

        Method getProcessingAppInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("getProcessingAppInfo");
        getProcessingAppInfo.setAccessible(true);
        Object invoke = getProcessingAppInfo.invoke(aggregationTaskCoordinator);
        Assertions.assertTrue(invoke.toString().equals(TEST0));


        Method removeAppInfoFromOrderList = aggregationTaskCoordinator.getClass().getDeclaredMethod("removeAppInfoFromOrderList", String.class);
        removeAppInfoFromOrderList.setAccessible(true);
        removeAppInfoFromOrderList.invoke(aggregationTaskCoordinator, TEST0);

        invoke = getProcessingAppInfo.invoke(aggregationTaskCoordinator);
        Assertions.assertTrue(invoke.toString().equals(TEST1));


        invoke = removeAppInfoFromOrderList.invoke(aggregationTaskCoordinator, TEST1);
        Assertions.assertTrue(invoke == null);

    }


    @Test
    public void testRunTask() throws Exception {
        MockTaskRunner runner = new MockTaskRunner(finishedTaskIds);
        aggregationTaskCoordinator.registerRunner(runner);

        aggregationTaskCoordinator.init();

        int size = 1000;
        CountDownLatch latch = new CountDownLatch(size);
        CountDownLatch latch1 = new CountDownLatch(size);
        List<String> exceptedTaskIds = new ArrayList<>(size);
        MockTask newTask;
        for (int i = 0; i < size; i++) {
            newTask = new MockTask(latch, TEST0);
            newTask.setPrefix(TEST0);
            exceptedTaskIds.add(newTask.id());
            Assertions.assertTrue(aggregationTaskCoordinator.addTask(TEST0, newTask));
        }

        long incr = kv.incr(String.format("%s-%s", TEST0, TaskKeyValueQueue.UNUSED), 0);
        Assertions.assertTrue(incr == size);

        Method addOrderInfo = aggregationTaskCoordinator.getClass().getDeclaredMethod("addOrderInfo", String.class);
        addOrderInfo.setAccessible(true);
        addOrderInfo.invoke(aggregationTaskCoordinator, TEST0);

        for (int i = 0; i < size; i++) {
            newTask = new MockTask(latch1, TEST1);
            newTask.setPrefix(TEST1);
            exceptedTaskIds.add(newTask.id());
            Assertions.assertTrue(aggregationTaskCoordinator.addTask(TEST1, newTask));
        }
        incr = kv.incr(String.format("%s-%s", TEST1, TaskKeyValueQueue.UNUSED), 0);
        Assertions.assertTrue(incr == size);
        addOrderInfo.invoke(aggregationTaskCoordinator, TEST1);

        latch.await();
        incr = kv.incr(String.format("%s-%s", TEST0, TaskKeyValueQueue.UNUSED), 0);
        Assertions.assertTrue(incr == 0);
        while (taskQueueMap.containsKey(TEST0)) {
            TimeUnit.MILLISECONDS.sleep(1000);
        }

        Assertions.assertTrue(kv.exist(TEST0));
        Assertions.assertTrue(!taskQueueMap.containsKey(TEST0));

        latch1.await();
        incr = kv.incr(String.format("%s-%s", TEST0, TaskKeyValueQueue.UNUSED), 0);
        Assertions.assertTrue(incr == 0);
        while (taskQueueMap.containsKey(TEST1)) {
            TimeUnit.MILLISECONDS.sleep(1000);
        }

        Assertions.assertTrue(kv.exist(TEST1));
        Assertions.assertTrue(!taskQueueMap.containsKey(TEST1));
        Assertions.assertTrue(taskQueueMap.isEmpty());
        Assertions.assertTrue(usingApp.isEmpty());


        Collections.sort(exceptedTaskIds);
        Collections.sort(finishedTaskIds);
        for (int i = 0; i < exceptedTaskIds.size(); i++) {
            Assertions.assertEquals(exceptedTaskIds.get(i), this.finishedTaskIds.get(i));
        }
    }


    class MockTaskQueue extends TaskKeyValueQueue {

        private BlockingQueue<Task> tasks = new LinkedBlockingQueue();
        private ConcurrentMap<String, Task> waitAckTasks = new ConcurrentHashMap<>();
        private String name;

        public List<Task> getTasks() {
            return new ArrayList<>(tasks);
        }

        public Map<String, Task> getWaitAckTasks() {
            return new HashMap<>(waitAckTasks);
        }

        public void setTasks(BlockingQueue<Task> tasks) {
            this.tasks = tasks;
        }

        public void setWaitAckTasks(ConcurrentMap<String, Task> waitAckTasks) {
            this.waitAckTasks = waitAckTasks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MockTaskQueue(String name) {
            this.name = name;
        }

        @Override
        public void append(Task task) {
            AggregationTask mockTask = (AggregationTask) task;
            kv.incr(String.format("%s-%s", mockTask.getPrefix(), TaskKeyValueQueue.UNUSED));
            tasks.add(task);
        }

        @Override
        public Task get() {
            AggregationTask task = null;
            try {
                task = (AggregationTask) tasks.take();
            } catch (InterruptedException e) {
                return null;
            } finally {
                if (task != null) {
                    kv.incr(String.format("%s-%s", task.getPrefix(), TaskKeyValueQueue.UNUSED), -1L);
                    waitAckTasks.put(task.id(), task);
                }
            }

            return task;
        }

        @Override
        public Task get(long awaitTimeMs) {
            AggregationTask task = null;
            try {
                task = (AggregationTask) tasks.poll(awaitTimeMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return null;
            } finally {
                if (task != null) {
                    kv.incr(String.format("%s-%s", task.getPrefix(), TaskKeyValueQueue.UNUSED), -1L);
                    waitAckTasks.put(task.id(), task);
                }
            }
            return task;
        }

        @Override
        public void ack(Task task) {
            waitAckTasks.remove(task.id());
        }

        @Override
        public void destroy() {

        }
    }

    class MockMemoryKV implements KeyValueStorage {
        private final byte[] emptyValue = new byte[0];
        private ConcurrentMap<String, byte[]> data;
        private ConcurrentMap<String, AtomicLong> numberData;


        public MockMemoryKV() {
            this.data = new ConcurrentSkipListMap<>();
            this.numberData = new ConcurrentHashMap<>();
        }

        @Override
        public void save(String key, byte[] value) {
            data.put(key, value == null ? emptyValue : value);
        }

        @Override
        public long save(Collection<Map.Entry<String, byte[]>> kvs) {
            kvs.stream().forEach(kv -> {
                data.put(kv.getKey(), kv.getValue() == null ? emptyValue : kv.getValue());
            });

            return kvs.size();
        }

        @Override
        public boolean add(String key, byte[] value) {
            return data.putIfAbsent(key, value == null ? emptyValue : value) == null;
        }

        @Override
        public boolean exist(String key) {
            return data.containsKey(key) || numberData.containsKey(key);
        }

        @Override
        public Optional<byte[]> get(String key) {
            byte[] value = data.get(key);
            if (Arrays.equals(value, emptyValue)) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(value);
            }
        }

        @Override
        public Collection<Map.Entry<String, byte[]>> get(String[] keys) {
            Collection<Map.Entry<String, byte[]>> datas = new ArrayList<>(keys.length);
            for (String key : keys) {
                byte[] value = data.get(key);
                if (Arrays.equals(value, emptyValue)) {
                    value = null;
                }
                if (value != null) {
                    datas.add(new AbstractMap.SimpleEntry<>(key, value));
                }
            }
            return datas;
        }

        @Override
        public void delete(String key) {
            data.remove(key);
        }

        @Override
        public void delete(String[] keys) {

            for (String key : keys) {
                data.remove(key);
            }
        }

        @Override
        public KeyIterator iterator(String keyPrefix, boolean asc) {
            return null;
        }

        @Override
        public long incr(String key, long step) {
            AtomicLong old = numberData.putIfAbsent(key, new AtomicLong(step));
            if (old != null) {
                return old.accumulateAndGet(step, (left, right) -> {
                    return left + right;
                });
            } else {

                return step;
            }
        }

        class MemoryKeyIterator implements KeyIterator {

            private String keyPrefix;
            private List<String> keys;
            private String lastKey;
            private int point = 0;
            private boolean asc;

            public MemoryKeyIterator(String keyPrefix, boolean asc) {
                this.keyPrefix = keyPrefix;
                this.asc = asc;

                this.keys = new ArrayList<>(data.keySet());
                if (this.asc) {

                    for (; point < keys.size(); ) {
                        if (keys.get(point).startsWith(keyPrefix)) {
                            break;
                        }
                        point++;
                    }

                } else {

                    for (this.point = this.keys.size() - 1; this.point >= 0; ) {
                        if (keys.get(point).startsWith(keyPrefix)) {
                            break;
                        }
                        point--;
                    }

                }
            }

            @Override
            public void seek(String key) {
                for (int i = 0; i < this.keys.size(); i++) {
                    if (this.keys.get(i).equals(key)) {
                        this.point = i;
                        break;
                    }
                }

                if (this.asc) {
                    this.point++;
                } else {
                    this.point--;
                }
            }

            @Override
            public String currentKey() {
                return this.lastKey;
            }

            @Override
            public boolean hasNext() {
                if (keys.isEmpty()) {
                    return false;
                }

                if (keys.size() - point == 0) {
                    return false;
                } else {
                    return this.keys.get(point).startsWith(keyPrefix);
                }
            }

            @Override
            public String next() {
                lastKey = keys.get(point);

                if (this.asc) {
                    this.point++;
                } else {
                    this.point--;
                }

                return lastKey;
            }
        }
    }

    static class MockTask extends AggregationTask {

        private CountDownLatch latch;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        private String prefix;

        public MockTask(CountDownLatch latch, String prefix) {
            super(prefix, new MetaParseTree());
            this.prefix = prefix;
            this.latch = latch;
        }

        @Override
        public long location() {
            return 1;
        }

        @Override
        public void setLocation(long l) {

        }

        @Override
        public Class runnerType() {
            return MockTaskRunner.class;
        }

        public void finish() {
            latch.countDown();
        }
    }

    static class MockTaskRunner implements TaskRunner {

        private List<String> finishedTaskIds;

        public MockTaskRunner(List<String> finishedTaskIds) {
            this.finishedTaskIds = finishedTaskIds;
        }

        @Override
        public void run(TaskCoordinator coordinator, Task task) {
            finishedTaskIds.add(task.id());

            ((MockTask) task).finish();
        }
    }
}