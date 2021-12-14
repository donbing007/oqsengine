package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import io.grpc.stub.StreamObserver;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class DoNothingRequestHandler implements IRequestHandler {
    @Override
    public boolean register(WatchElement watchElement) {
        return true;
    }

    @Override
    public boolean reRegister() {
        return true;
    }

    @Override
    public void initWatcher(String clientId, String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {

    }

    @Override
    public IRequestWatchExecutor watchExecutor() {
        return null;
    }

    @Override
    public void notReady() {

    }

    @Override
    public void ready() {

    }

    @Override
    public boolean reset(WatchElement watchElement) {
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void unused) {

    }

    @Override
    public boolean isShutDown() {
        return false;
    }
}
