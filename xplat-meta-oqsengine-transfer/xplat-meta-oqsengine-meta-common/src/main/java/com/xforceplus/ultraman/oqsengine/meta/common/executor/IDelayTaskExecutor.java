package com.xforceplus.ultraman.oqsengine.meta.common.executor;

/**
 * interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IDelayTaskExecutor<T> extends IBasicSyncExecutor {

    T take();

    void offer(T task);
}
