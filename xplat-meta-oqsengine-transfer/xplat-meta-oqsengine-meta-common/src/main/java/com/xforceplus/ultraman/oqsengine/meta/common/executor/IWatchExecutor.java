package com.xforceplus.ultraman.oqsengine.meta.common.executor;


/**
 * desc :
 * name : IWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IWatchExecutor extends IBasicSyncExecutor {
    /**
     * 重置心跳
     * @param uid
     */
    void resetHeartBeat(String uid);

    /**
     * 释放当前Uid的RequestWatcher
     * @param uid
     */
    void release(String uid);
}
