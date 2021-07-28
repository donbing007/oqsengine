package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * response watcher.
 *
 * @author xujia
 * @since 1.8
 */
public class ResponseWatcher extends AbstractWatcher<EntityClassSyncResponse> {

    private final Logger logger = LoggerFactory.getLogger(ResponseWatcher.class);

    public ResponseWatcher(String clientId, String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        super(clientId, uid, streamObserver);
    }

    @Override
    public boolean onWatch(WatchElement w) {
        WatchElement v = watches.get(w.getAppId());
        return null == v || (v.getEnv().equals(w.getEnv()) && v.getVersion() < w.getVersion());
    }

    @Override
    public boolean isAlive(String uid) {
        return uid.equals(this.uid) && isActive();
    }

    @Override
    public void reset(String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        throw new MetaSyncServerException("un-support function reset.", false);
    }

    @Override
    public void release() {
        logger.warn("release response watcher uid [{}]", uid);
        releaseStreamObserver();
    }
}
