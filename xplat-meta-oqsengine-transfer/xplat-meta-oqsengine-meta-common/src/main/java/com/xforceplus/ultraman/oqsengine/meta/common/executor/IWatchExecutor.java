package com.xforceplus.ultraman.oqsengine.meta.common.executor;


/**
 * interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IWatchExecutor extends IBasicSyncExecutor {
    /**
     * 重置心跳.
     */
    void resetHeartBeat(String uid);

    /**
     * 释放当前Uid的RequestWatcher.
     */
    void release(String uid);
}
