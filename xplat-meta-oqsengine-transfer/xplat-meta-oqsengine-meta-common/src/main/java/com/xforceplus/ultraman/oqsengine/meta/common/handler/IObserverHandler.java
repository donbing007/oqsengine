package com.xforceplus.ultraman.oqsengine.meta.common.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;

/**
 * desc :
 * name : IObserverHandler
 *
 * @author : xujia
 * date : 2021/3/12
 * @since : 1.8
 */
public interface IObserverHandler<T, Q> extends IBasicSyncExecutor {
    void onNext(T t, Q q);

    boolean isShutDown();
}
