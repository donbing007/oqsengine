package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.task.Task;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 用以测试的基于内存实现的任务队列.
 *
 * @author dongbin
 * @version 0.1 2021/08/17 15:56
 * @since 1.8
 */
public class MemoryTaskKeyQueue implements TaskQueue {

    private BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    @Override
    public void append(Task task) {
        queue.add(task);
    }

    @Override
    public Task get() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Task get(long awaitTimeMs) {
        try {
            return queue.poll(awaitTimeMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void ack(Task task) {

    }
}
