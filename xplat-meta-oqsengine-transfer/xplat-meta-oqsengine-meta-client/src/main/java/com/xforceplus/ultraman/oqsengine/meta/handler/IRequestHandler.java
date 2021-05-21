package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.handler.IObserverHandler;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import io.grpc.stub.StreamObserver;

import java.util.List;

/**
 * desc :
 * name : EntityClassExecutor
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public interface IRequestHandler extends IObserverHandler<EntityClassSyncResponse, Void> {

    /**
     * 注册一个appId，并开始监听
     * @return boolean
     */
    boolean register(WatchElement watchElement);

    /**
     * 断流自动重新注册
     * @return boolean
     */
    boolean reRegister();

    /**
     *
     */
    void initWatcher(String uid, StreamObserver<EntityClassSyncRequest> streamObserver);

    /**
     * 获得当前IRequestWatchExecutor
     * @return
     */
    IRequestWatchExecutor watchExecutor();

    /**
     * 服务未准备就绪
     */
    void notReady();

    /**
     * 服务准备就绪
     */
    void ready();

}
