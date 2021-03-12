package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;


/**
 * desc :
 * name : ResponseWatcher
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatcher extends AbstractWatcher<EntityClassSyncResponse> {

    public ResponseWatcher(String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        super(uid, streamObserver);
    }

    @Override
    public boolean onWatch(WatchElement w) {
        WatchElement v = watches.get(w.getAppId());
        return null == v || v.getVersion() < w.getVersion();
    }

    @Override
    public boolean isAlive(String uid) {
        return uid.equals(this.uid) && isOnServe();
    }

    @Override
    public void reset(String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        throw new MetaSyncServerException("un-support function reset.", false);
    }
}
