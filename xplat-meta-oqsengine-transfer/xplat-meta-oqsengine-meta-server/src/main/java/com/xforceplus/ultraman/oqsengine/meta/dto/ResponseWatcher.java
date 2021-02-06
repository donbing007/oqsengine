package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.function.Function;

/**
 * desc :
 * name : ResponseWatcher
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class ResponseWatcher extends AbstractWatcher<EntityClassSyncResponse, Integer> {


    public ResponseWatcher(String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        super(uid, streamObserver);

    }

    @Override
    public boolean onWatch(String appId, Integer version) {
        Integer v = watches.get(appId);
        return null == v || v < version;
    }

    @Override
    public Map<String, Integer> watches() {
        return watches;
    }


    @Override
    public void release() {
        try {
            if (null != streamObserver) {
                streamObserver.onCompleted();
            }
        } catch (Exception e) {
            //  ignore
        }

        watches.clear();
    }

    @Override
    public void reset(String uid, StreamObserver<EntityClassSyncResponse> streamObserver) {
        throw new MetaSyncServerException("un-support function reset.", false);
    }

    public boolean runWithCheck(Function<StreamObserver<EntityClassSyncResponse>, Boolean> function) {

        if (!isRemoved) {
            return function.apply(streamObserver);
        }
        return false;
    }

}
