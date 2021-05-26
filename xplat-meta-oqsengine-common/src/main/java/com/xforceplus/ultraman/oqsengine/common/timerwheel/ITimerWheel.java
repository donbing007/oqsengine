package com.xforceplus.ultraman.oqsengine.common.timerwheel;

/**
 * @param <T> 管理的元素类型.
 * @author weikai
 * @data 2021/5/21 15:50
 * @mail weikai@xforceplus.com
 */
public interface ITimerWheel<T>{

    /**
     * @param transaction
     * @param timeoutMs
     */
    void add(T transaction, long timeoutMs);

    /**
     * @param tx
     */
    void remove(T tx);
}
