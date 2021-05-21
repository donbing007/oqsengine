package com.xforceplus.ultraman.oqsengine.meta.common.executor;

/**
 * desc :
 * name : IDelayTaskExecutor
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public interface IDelayTaskExecutor<T> extends IBasicSyncExecutor {

    T take();

    void offer(T task);
}
