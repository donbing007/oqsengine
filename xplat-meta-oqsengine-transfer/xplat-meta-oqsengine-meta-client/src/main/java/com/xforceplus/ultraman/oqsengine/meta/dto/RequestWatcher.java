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
    public boolean onWatch(WatchElement watchElement) {
        WatchElement v = watches.get(watchElement.getAppId());
        if (null == v) {
            return false;
        }

        /**
         * 当前版本小于输入版本或当前版本
         */
        return v.getVersion() < watchElement.getVersion() ||
                (v.getVersion() == watchElement.getVersion() && v.getStatus().ordinal() < watchElement.getStatus().ordinal());
    }

    @Override
    public boolean isAlive(String uid) {
        /**
         * 判断是否可用
         */
        if (null != uid && isActive()) {
            try {
                return uid.equals(this.uid());
            } catch (Exception e) {
                /**
                 * 兜底瞬间将uid置为null的逻辑.
                 */
                return false;
            }
        }
        return false;
    }

    @Override
    public void release() {
        uid = null;
        super.release();
    }
}
