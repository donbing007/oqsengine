package com.xforceplus.ultraman.oqsengine.common.timerwheel;

/**
 * 时间轮接口.
 *
 * @param <T> 管理的元素类型.
 * @author weikai
 * @version 1.0 2021/5/21 15:50
 * @since 1.5
 */
public interface ITimerWheel<T> {

    /**
     * 添加任务.
     *
     * @param transaction 超时通知
     * @param timeoutMs 超时时间
     */
    void add(T transaction, long timeoutMs);

    /**
     * 删除任务.
     *
     * @param tx 待删除任务
     */
    void remove(T tx);
}
