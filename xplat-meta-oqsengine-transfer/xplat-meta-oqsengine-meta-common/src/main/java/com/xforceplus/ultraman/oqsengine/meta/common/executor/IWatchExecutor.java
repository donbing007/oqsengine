package com.xforceplus.ultraman.oqsengine.meta.common.executor;


/**
 * desc :
 * name : IWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IWatchExecutor {

    void start();

    void stop();

    void heartBeat(String uid);
}
