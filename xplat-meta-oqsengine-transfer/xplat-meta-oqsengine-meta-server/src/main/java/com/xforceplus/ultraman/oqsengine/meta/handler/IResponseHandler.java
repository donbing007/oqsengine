package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.handler.IObserverHandler;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;
import io.grpc.stub.StreamObserver;

/**
 * response handler interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IResponseHandler extends IObserverHandler<EntityClassSyncRequest, StreamObserver<EntityClassSyncResponse>> {
    /**
     * 拉取.
     */
    void pull(String clientId, String uid, boolean force, WatchElement watchElement, RequestStatus requestStatus);

    /**
     * 推送.
     */
    boolean push(ServerSyncEvent event);
}

