package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;

/**
 * request watch executor interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IRequestWatchExecutor extends IWatchExecutor {

    /**
     * 创建一个新的RequestWatcher.
     */
    void create(String clientId, String uid, StreamObserver<EntityClassSyncRequest> observer);

    /**
     * 增加WatchElement.
     */
    void add(WatchElement watchElement);

    /**
     * 更新WatchElement.
     */
    boolean update(WatchElement watchElement);

    /**
     * 当前RequestWatcher是否处于活动状态.
     */
    boolean isAlive(String uid);

    /**
     * 返回当前RequestWatcher实例.
     */
    RequestWatcher watcher();

    /**
     * 激活RequestWatcher.
     */
    void active();

    /**
     * 禁用RequestWatcher.
     */
    void inActive();
}
