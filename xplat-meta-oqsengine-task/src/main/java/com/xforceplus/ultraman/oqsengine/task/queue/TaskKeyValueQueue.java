package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
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
public class TaskKeyValueQueue implements TaskQueue {

    private static final String DEFAULT_NAME = "default";
    private Logger logger = LoggerFactory.getLogger(TaskKeyValueQueue.class);

    @Resource
    private ResourceLocker locker;

    @Resource
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
    private static final String UNUSED = "unused";

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
    private String unusedTask;

    private volatile boolean running;

    private volatile boolean exit;

    private long syncGapTime;

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

        this.pointKey = String.format("%s-%s", this.name, POINT_KEY);
        this.unusedTask = String.format("%s-%s", this.name, UNUSED);
        this.elementKeyPrefix = String.format("%s-%s", this.name, ELEMENT_KEY);
        this.initPoint = initPoint;
        this.running = true;
        this.exit = false;
        this.syncGapTime = 30L;

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
    public void destroy() {
        running = false;
        while (!exit) {

        }
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

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
        // 第一个元素.
        if (elementId == initPoint) {
            kv.incr(pointKey, 0);
            kv.incr(pointKey, initPoint - 1);
        }
        task.setLocation(elementId);
        String elementKey = buildNextElementKey(elementId);
        unSubmitTask.put(elementKey, serializeStrategy.serialize(task));
    }

    @Override
    public Task get() {
        checkRunning();
        try {
            locker.lock(anyLock);
            while (true) {
                if (kv.incr(unusedTask, 0) <= 0) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                } else {
                    break;
                }
            }
            kv.incr(unusedTask, -1L);
        } catch (RuntimeException runtimeException) {
            logger.error("incr unusedTask {} failed when get task", unusedTask);
            return null;
        } finally {
            locker.unlock(anyLock);
        }
        return getTask();
    }


    @Override
    public Task get(long awaitTimeMs) {
        checkRunning();
        try {
            long timeMillis = System.currentTimeMillis();
            locker.lock(anyLock);
            while (true) {
                if (System.currentTimeMillis() - timeMillis >= awaitTimeMs) {
                    return null;
                }
                if (kv.incr(unusedTask, 0) <= 0) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                } else {
                    break;
                }
            }
            kv.incr(unusedTask, -1L);
        } catch (RuntimeException runtimeException) {
            logger.error("incr unusedTask {} failed when get task", unusedTask);
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
            nextElementId = kv.incr(pointKey);
        } catch (RuntimeException e) {
            logger.error("incr pointKey {} failed when get task", pointKey);
            kv.incr(unusedTask);
            throw new RuntimeException(String.format("pointKey %s incr failed. ", pointKey));
        }
        String elementKey = buildNextElementKey(nextElementId);

        int count = 0;
        while (count <= 3) {
            task = getTask(elementKey);
            if (task != null) {
                break;
            } else {
                if (count++ >= 3) {
                    throw new RuntimeException(String.format("Task not found where elementKey equals %s .", elementKey));
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
            task = (Task) serializeStrategy.unserialize(optionalBytes.get());
            return task;
        }
        return null;
    }

    @Override
    public void ack(Task task) {
        checkRunning();
        if (task == null) {
            return;
        }
        long location = task.location();
        int count = 0;
        while (count <= 3) {
            try {
                kv.delete(buildNextElementKey(location));
                break;
            } catch (Exception e) {
                if (count++ >= 3) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(String.format("Task ack failed taskLocation = %s", buildNextElementKey(location)));
                } else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                }
            }
        }
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
            ConcurrentHashMap temp;
            Set<Map.Entry<String, byte[]>> tempSet;

            while (running) {
                try {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(syncGapTime));
                    if (unSubmitTask.size() > 0) {
                        temp = new ConcurrentHashMap(unSubmitTask);
                        tempSet = temp.entrySet();
                        kv.save(tempSet);
                        kv.incr(unusedTask, temp.size());
                        for (Map.Entry<String, byte[]> entry : tempSet) {
                            unSubmitTask.remove(entry.getKey());
                        }
                    }
                } catch (RuntimeException e) {
                    logger.error(String.format("kv error when batchSave or incr unusedTask.source[{}]", e.getMessage()), e);
                }
            }

            exit = true;
        }
    }

}
