package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一个基于key-value储存的队列实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/05 14:22
 * @since 1.8
 */
public class TaskKeyValueQueue implements TaskQueue, Lifecycle {

    private static final String DEFAULT_NAME = "default";
    private Logger logger = LoggerFactory.getLogger(TaskKeyValueQueue.class);

    @Resource
    private ResourceLocker locker;

    @Resource(name = "longContinuousPartialOrderIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    private ExecutorService worker;


    /**
     * 队列元素key.
     */
    private static final String ELEMENT_KEY = "task-queue-e";
    /**
     * 当前指针序号(未取出指针).
     */
    private static final String POINT_KEY = "task-queue-p";

    /**
     * 未使用任务标识.
     */
    public static final String UNUSED = "unused";

    private String anyLock;

    /**
     * init point.
     */
    private long initPoint;


    /**
     * 内存暂存任务列表.
     */
    private ConcurrentHashMap<String, byte[]> unSubmitTask;


    /**
     * 队列名称.
     */
    private String name;
    /**
     * 当前序号的key.
     */
    private String pointKey;
    /**
     * 队列元素key的前辍.
     */
    private String elementKeyPrefix;

    /**
     * unused task size in queue.
     */
    private String unusedTaskSize;

    private volatile boolean running;

    private CountDownLatch latch;

    private long syncGapTimeMs;

    public TaskKeyValueQueue() {
        this(DEFAULT_NAME);
    }

    /**
     * 初始化.
     *
     * @param name 队列名称.
     */
    public TaskKeyValueQueue(String name) {
        this(name, 1L);
    }

    /**
     * 初始化.
     *
     * @param name      队列名称.
     * @param initPoint .
     */
    public TaskKeyValueQueue(String name, long initPoint) {
        this.name = name;
        this.anyLock = "anyLock-" + name;
        this.initPoint = initPoint;
    }

    @PostConstruct
    @Override
    public void init() throws Exception {
        this.pointKey = String.format("%s-%s", this.name, POINT_KEY);
        this.unusedTaskSize = String.format("%s-%s", this.name, UNUSED);
        this.elementKeyPrefix = String.format("%s-%s", this.name, ELEMENT_KEY);
        this.running = true;
        this.latch = new CountDownLatch(1);
        this.syncGapTimeMs = 30L;

        this.unSubmitTask = new ConcurrentHashMap();
        worker = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue(10000),
            ExecutorHelper.buildNameThreadFactory("task", false),
            new ThreadPoolExecutor.AbortPolicy()
        );
        worker.submit(new TaskBatchSave());
    }

    /**
     * 停止append任务,结束插入磁盘任务.
     */
    @PreDestroy
    public void destroy() {
        running = false;
        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // 不做处理
        }
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    /**
     * 模拟断电，仅供测试使用.
     */
    private void destroyNow() {
        running = false;
        worker.shutdownNow();
    }

    @Override
    public void append(Task task) {
        checkRunning();
        if (task == null) {
            return;
        }
        long elementId = nextId();
        /*
            判断队列是否第一次添加任务，初始化待取出指针位置，指向当前任务前一个索引。
         */
        if (elementId == initPoint) {
            kv.incr(pointKey, 0);
            kv.incr(pointKey, initPoint - 1);
        }
        task.setLocation(elementId);
        String elementKey = buildNextElementKey(elementId);
        // 将任务暂存在内存中，经过syncGapTimeMs时长后持久化
        unSubmitTask.put(elementKey, serializeStrategy.serialize((Serializable) task));
    }

    @Override
    public Task get() {
        return get(Long.MAX_VALUE);
    }


    @Override
    public Task get(long awaitTimeMs) {
        checkRunning();
        try {
            long timeMillis = System.currentTimeMillis();
            locker.lock(anyLock);
            /*
                判断当前队列是否有任务，没有任务则等待awaitTimeMs
             */
            while (true) {
                if (System.currentTimeMillis() - timeMillis >= awaitTimeMs) {
                    return null;
                }
                if (kv.incr(unusedTaskSize, 0) <= 0) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000L));
                } else {
                    break;
                }
            }
            // 取出任务后将未取出任务数减一
            kv.incr(unusedTaskSize, -1L);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        } finally {
            locker.unlock(anyLock);
        }
        return getTask();
    }

    private Task getTask() {
        Task task = null;
        long nextElementId;
        try {
            // 待取出任务索引值加一
            nextElementId = kv.incr(pointKey);
        } catch (RuntimeException e) {
            logger.error("incr pointKey {} failed when get task", pointKey);
            kv.incr(unusedTaskSize);
            throw new RuntimeException(String.format("pointKey %s incr failed. ", pointKey));
        }
        String elementKey = buildNextElementKey(nextElementId);

        /*
        取出任务
         */
        int count = 0;
        while (count <= 3) {
            task = getTask(elementKey);
            if (task != null) {
                break;
            } else {
                if (count++ >= 3) {
                    throw new RuntimeException(
                        String.format("Task not found where elementKey equals %s .", elementKey));
                } else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                }
            }
        }
        return task;
    }

    private Task getTask(String elementKey) {
        Task task;
        Optional<byte[]> optionalBytes = kv.get(elementKey);
        if (optionalBytes.isPresent()) {
            task = serializeStrategy.unserialize(optionalBytes.get(), Task.class);
            return task;
        }
        return null;
    }

    @Override
    public String toString() {
        return "TaskKeyValueQueue{" + "name='" + name + '\'' + '}';
    }

    @Override
    public void ack(Task task) {
        checkRunning();
        if (task == null) {
            return;
        }
        long location = task.location();
        /*
            从磁盘中删除任务
         */
        int count = 0;
        while (count <= 3) {
            try {
                kv.delete(buildNextElementKey(location));
                break;
            } catch (Exception e) {
                if (count++ >= 3) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(
                        String.format("Task ack failed taskLocation = %s", buildNextElementKey(location)));
                } else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                }
            }
        }
    }

    public boolean isEmpty() {
        return kv.incr(unusedTaskSize, 0) <= 0;
    }


    private String buildNextElementKey(long id) {
        StringBuilder buff = new StringBuilder();
        buff.append(this.elementKeyPrefix)
            .append('-')
            .append(id);
        return buff.toString();
    }

    private long nextId() {
        if (idGenerator.supportNameSpace() && idGenerator.isContinuous()) {
            return idGenerator.next(name);
        } else {
            logger.error(idGenerator.getClass().getName() + " do not support namespace ");
            throw new RuntimeException(idGenerator.getClass().getName() + " do not support namespace ");
        }
    }

    private void checkRunning() {
        if (!running) {
            throw new RuntimeException("当前任务队列不可用");
        }
    }

    private class TaskBatchSave implements Runnable {

        @Override
        public void run() {
            HashMap temp;
            Set<Map.Entry<String, byte[]>> tempSet;

            /*
                将内存暂存任务持久化至磁盘，每syncGapTimeMs时长同步一次
             */
            while (running) {
                try {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(syncGapTimeMs));
                    if (unSubmitTask.size() > 0) {
                        temp = new HashMap(unSubmitTask);
                        tempSet = temp.entrySet();
                        kv.save(tempSet);
                        kv.incr(unusedTaskSize, temp.size());
                        for (Map.Entry<String, byte[]> entry : tempSet) {
                            unSubmitTask.remove(entry.getKey());
                        }
                    }
                } catch (RuntimeException e) {
                    logger.error(String.format("kv error when batchSave or incr unusedTask.source[{}]", e.getMessage()),
                        e);
                }
            }

            latch.countDown();
        }
    }

}
