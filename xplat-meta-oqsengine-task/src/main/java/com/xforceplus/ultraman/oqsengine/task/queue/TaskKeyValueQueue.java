package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Resource;

/**
 * 一个基于key-value储存的队列实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/05 14:22
 * @since 1.8
 */
public class TaskKeyValueQueue implements TaskQueue {

    @Resource
    private ResourceLocker locker;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    private static final String INDEX_KEY = "task-queue-i";
    /**
     * 队列元素key.
     */
    private static final String ELEMENT_KEY = "task-queue-e";
    /**
     * 当前指针序号.
     */
    private static final String POINT_KEY = "task-queue-p";

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
     * 队列当前最大序号.
     */
    private String indexKey;

    /**
     * 初始化.
     *
     * @param name 队列名称.
     * @param kv   kv储存实例.
     */
    public TaskKeyValueQueue(String name, KeyValueStorage kv) {
        this.kv = kv;
        this.name = name;

        this.pointKey = String.format("%s-%s", this.name, POINT_KEY);
        this.elementKeyPrefix = String.format("%s-%s", this.name, ELEMENT_KEY);
        this.indexKey = String.format("%s-%s", this.name, INDEX_KEY);


        if (!kv.exist(pointKey)) {

            updatePointKey(-1L);

            kv.save(indexKey, ByteUtil.longToByte(-1L));
        }


    }

    @Override
    public void append(Task task) {
        long elementId = nextId();
        String elementKey = buildNextElementKey(elementId);
        kv.save(elementKey, serializeStrategy.serialize(task));

        // 第一个元素.
        if (elementId == 0) {

        }
    }

    @Override
    public Task get() {
        kv.iterator()
    }

    @Override
    public Task get(long awaitTimeMs) {
        return null;
    }


    private String buildNextElementKey(long id) {
        StringBuilder buff = new StringBuilder();
        buff.append(this.elementKeyPrefix)
            .append('-')
            .append(id);

        return buff.toString();
    }

    private long nextId() {
        Optional<byte[]> indexOp;
        indexOp = kv.get(indexKey);

        long index = -1;
        if (indexOp.isPresent()) {
            index = ByteUtil.byteToLong(indexOp.get());
        }

        index++;

        kv.save(indexKey, ByteUtil.longToByte(index));


        return index;
    }



    private void updatePointKey(long point) {
        kv.save(pointKey, ByteUtil.longToByte(point));
    }

    private void updateIndexKey(long index) {
        kv.save(indexKey, ByteUtil.longToByte(index));
    }

}
