package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个基于key-value储存的队列实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/05 14:22
 * @since 1.8
 */
public class TaskKeyValueQueue implements TaskQueue {

    private static final String DEFAULT_NAME = "default";

    @Resource
    private ResourceLocker locker;

    @Resource
    private IdGenerator idGenerator;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    /**
     * 队列元素key.
     */
    private static final String ELEMENT_KEY = "task-queue-e";
    /**
     * 当前指针序号(未取出指针).
     */
    private static final String POINT_KEY = "task-queue-p";

    private String anyLock;

    private long size;

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

    public TaskKeyValueQueue() {
        this(DEFAULT_NAME);
    }

    /**
     * 初始化.
     *
     * @param name 队列名称.
     */
    public TaskKeyValueQueue(String name) {
        this.name = name;
        this.anyLock = "anyLock-" + name;

        this.pointKey = String.format("%s-%s", this.name, POINT_KEY);
        this.elementKeyPrefix = String.format("%s-%s", this.name, ELEMENT_KEY);
    }

    @Override
    public void append(Task task) throws Exception {
        if (task == null) {
            return;
        }
        long elementId = nextId();
        task.setLocation(elementId);
        String elementKey = buildNextElementKey(elementId);
        kv.save(elementKey, serializeStrategy.serialize(task));
        // 第一个元素.
        if (elementId == 0) {
            kv.incr(pointKey, 0);
        }
        incrSize();
    }

    @Override
    public Task get() throws Exception {
        if (!hasNext()){
            return null;
        }
        Task task = null;
        long nextElementId;
        try {
            nextElementId = kv.incr(pointKey);
        } catch (Exception e) {
            throw new Exception(String.format("pointKey %s incr failed. ", pointKey));
        }
        String elementKey = buildNextElementKey(nextElementId - 1);

        int count = 0;
        while (count <= 3) {
            task = getTask(elementKey);
            if (task != null) {
                descSize();
                break;
            } else {
                if (count++ >= 3) {
                    throw new Exception(String.format("Task not found where elementKey equals %s .", elementKey));
                } else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                }
            }
        }
        return task;
    }


    @Override
    public Task get(long awaitTimeMs) throws Exception {
        return get();
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
    public void ack(Task task) throws Exception {
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
                if (count++ >= 3){
                    throw new Exception(String.format("Task ack failed taskLocation = %s",buildNextElementKey(location)));
                }else {
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

    private long nextId() throws Exception {
        if (idGenerator.supportNameSpace()) {
            return (long) idGenerator.next(name);
        }else {
            throw new Exception(idGenerator.getClass().getName() + "idGenerator do not support namespace ");
        }
    }

    private boolean hasNext() {
        return size > 0 ? true:false;
    }

    private void incrSize(){
        try {
            locker.lock(anyLock);
            size++;
        }finally {
            locker.unlock(anyLock);
        }
    }

    private void descSize(){
        try {
            locker.lock(anyLock);
            size--;
        }finally {
            locker.unlock(anyLock);
        }
    }

}
