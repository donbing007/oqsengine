package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import io.grpc.stub.StreamObserver;

/**
 * desc :
 * name : Watcher
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public class RequestWatcher extends AbstractWatcher<EntityClassSyncRequest> {

    public RequestWatcher(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        super(uid, observer);
    }

    public void reset(String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {
        super.uid = uid;
        super.streamObserver = streamObserver;
        super.heartBeat = System.currentTimeMillis();
        watches().values().forEach(WatchElement::reset);
    }

    @Override
    public StreamObserver<EntityClassSyncRequest> observer() {
        return streamObserver;
    }

    @Override
    public boolean onWatch(WatchElement watchElement) {
        WatchElement v = watches.get(watchElement.getAppId());
        if (null == v) {
            return true;
        }

        /**
         * 当前版本小于输入版本或当前版本相等时未确认
         */
        return v.getVersion() < watchElement.getVersion() ||
                (v.getVersion() == watchElement.getVersion() && v.getStatus() != WatchElement.AppStatus.Confirmed);
    }

    @Override
    public void release() {
        try {
            uid = null;
            if (null != streamObserver) {
                streamObserver.onCompleted();
            }
        } catch (Exception e) {
            //  ignore
        }
    }
}
