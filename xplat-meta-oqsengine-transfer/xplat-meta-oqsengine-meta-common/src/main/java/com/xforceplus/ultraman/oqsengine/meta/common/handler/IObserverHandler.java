package com.xforceplus.ultraman.oqsengine.meta.common.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;

/**
 * interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IObserverHandler<T, Q> extends IBasicSyncExecutor {
    /**
     * 执行对REQ/RSP的处理.
     */
    void invoke(T t, Q q);

    /**
     * handler是否已处于关闭状态.
     */
    boolean isShutDown();
}
