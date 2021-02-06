package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.function.Function;

/**
 * desc :
 * name : Watcher
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public class RequestWatcher extends AbstractWatcher<EntityClassSyncRequest, StatusElement> {

    public RequestWatcher(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        super(uid, observer);
    }

    public void reset(String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {
        super.uid = uid;
        super.streamObserver = streamObserver;
        super.heartBeat = System.currentTimeMillis();
        watches().values().forEach(StatusElement::reset);
        super.isRemoved = false;
    }

    @Override
    public void canServer() {
        super.isRemoved = true;
    }

    @Override
    public String uid() {
        return uid;
    }

    @Override
    public long heartBeat() {
        return heartBeat;
    }

    @Override
    public void resetHeartBeat() {
        heartBeat = System.currentTimeMillis();
    }

    @Override
    public StreamObserver<EntityClassSyncRequest> observer() {
        return streamObserver;
    }

    @Override
    public boolean onWatch(String appId, Integer version) {
        StatusElement v = watches.get(appId);
        if (null == v) {
            return true;
        }

        /**
         * 当前版本小于输入版本或当前版本相等时未确认
         */
        return v.getVersion() < version ||
                (v.getVersion() == version && StatusElement.Status.Confirmed != v.getStatus());
    }


    @Override
    public Map<String, StatusElement> watches() {
        return watches;
    }

    @Override
    public boolean runWithCheck(Function function) {
        return false;
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
