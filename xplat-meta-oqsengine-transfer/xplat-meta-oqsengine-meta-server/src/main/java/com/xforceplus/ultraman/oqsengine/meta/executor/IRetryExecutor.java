package com.xforceplus.ultraman.oqsengine.meta.executor;

/**
 * desc :
 * name : IRetryExecutor
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public interface IRetryExecutor {

    RetryExecutor.DelayTask take();

    void offer(RetryExecutor.DelayTask task);
}
