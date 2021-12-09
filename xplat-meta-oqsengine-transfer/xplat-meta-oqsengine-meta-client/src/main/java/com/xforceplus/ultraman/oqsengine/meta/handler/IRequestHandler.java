package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.handler.IObserverHandler;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import io.grpc.stub.StreamObserver;

/**
 * request handler interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IRequestHandler extends IObserverHandler<EntityClassSyncResponse, Void> {

    /**
     * 注册一个appId，并开始监听.
     */
    boolean register(WatchElement watchElement);

    /**
     * 断流自动重新注册.
     */
    boolean reRegister();

    /**
     * 初始化.
     */
    void initWatcher(String clientId, String uid, StreamObserver<EntityClassSyncRequest> streamObserver);

    /**
     * 获得当前IRequestWatchExecutor.
     */
    IRequestWatchExecutor watchExecutor();

    /**
     * 服务未准备就绪.
     */
    void notReady();

    /**
     * 服务准备就绪.
     */
    void ready();

    /**
     * 刷新一个watchElement的状态
     */
    boolean reset(WatchElement watchElement);

}
