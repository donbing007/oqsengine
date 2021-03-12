package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * desc :
 * name : ResponseWatcher
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatcher extends AbstractWatcher<EntityClassSyncResponse> {

    private Logger logger = LoggerFactory.getLogger(ResponseWatcher.class);

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

    @Override
    public void release() {
        logger.warn("release response watcher uid [{}]", uid);
        super.release();
    }
}
