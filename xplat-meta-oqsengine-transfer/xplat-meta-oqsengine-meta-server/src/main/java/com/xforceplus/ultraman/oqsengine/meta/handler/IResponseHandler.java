package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.handler.IObserverHandler;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import io.grpc.stub.StreamObserver;

/**
 * desc :
 * name : ResponseHandler
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IResponseHandler extends IObserverHandler<EntityClassSyncRequest, StreamObserver<EntityClassSyncResponse>> {
    /**
     *
     * @param uid
     * @param watchElement
     * @param requestStatus
     */
    void pull(String uid, boolean force, WatchElement watchElement, RequestStatus requestStatus);

    boolean push(AppUpdateEvent event);
}

