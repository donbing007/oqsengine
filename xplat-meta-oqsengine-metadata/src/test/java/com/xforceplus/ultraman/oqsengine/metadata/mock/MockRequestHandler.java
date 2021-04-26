package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.*;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageBuilder;
import io.grpc.stub.StreamObserver;

import javax.annotation.Resource;


import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : MockRequestHandler
 *
 * @author : xujia
 * date : 2021/2/20
 * @since : 1.8
 */
public class MockRequestHandler implements IRequestHandler {

    private static final long mockResponseTimeDuration = 5_000;

    public static final int EXIST_MIN_VERSION = 0;

    @Resource(name = "grpcSyncExecutor")
    private SyncExecutor syncExecutor;


    @Override
    public boolean register(WatchElement watchElement) {

        try {
            Thread.sleep(mockResponseTimeDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (watchElement.getVersion() == NOT_EXIST_VERSION) {
            watchElement.setVersion(EXIST_MIN_VERSION);
        }

        invoke(EntityClassStorageBuilder.entityClassSyncResponseGenerator(watchElement.getAppId(), watchElement.getVersion(),
                                                        EntityClassStorageBuilder.mockSelfFatherAncestorsGenerate(System.currentTimeMillis())), null);
        return true;
    }

    @Override
    public boolean reRegister() {
        return false;
    }

    @Override
    public void initWatcher(String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {

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
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void aVoid) {
        syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(),
                entityClassSyncResponse.getEntityClassSyncRspProto());
    }

    @Override
    public boolean isShutDown() {
        return false;
    }
}
