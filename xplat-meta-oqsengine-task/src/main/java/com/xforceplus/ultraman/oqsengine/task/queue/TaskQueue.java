package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.task.Task;

/**
 * 任务队列.
 *
 * @author dongbin
 * @version 0.1 2021/08/05 15:27
 * @since 1.8
 */
public interface TaskQueue extends Lifecycle {

    /**
     * 追加任务.
     *
     * @param task 任务.
     */
    public void append(Task task);

    /**
     * 获取队列中的第一个任务.<br>
     * 没有任务将一直阻塞直到可以获取任务.<br>
     * 被获取的任务将从队列中被删除.<br>
     *
     * @return 任务.
     */
    public Task get();

    /**
     * 获取队列中的第一个任务.<br>
     * 没有任务将阻塞直到批定的时间.<br>
     * 被获取的任务将从队列中被删除.<br>
     *
     * @param awaitTimeMs 等待的毫秒数.
     * @return 任务.
     */
    public Task get(long awaitTimeMs) throws InterruptedException;

    /**
     * 任务完成确认.
     *
     * @param task 任务.
     */
    public void ack(Task task);

}
