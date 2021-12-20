package com.xforceplus.ultraman.oqsengine.metadata.mock;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import io.grpc.stub.StreamObserver;
import javax.annotation.Resource;

/**
 * desc :.
 * name : MockRequestHandler
 *
 * @author : xujia 2021/2/20
 * @since : 1.8
 */
public class MockRequestHandler implements IRequestHandler {

    private static final long MOCK_RESPONSE_TIME_DURATION = 5_000;

    public static final int EXIST_MIN_VERSION = 0;

    @Resource(name = "grpcSyncExecutor")
    private SyncExecutor syncExecutor;


    @Override
    public boolean register(WatchElement watchElement) {

        try {
            Thread.sleep(MOCK_RESPONSE_TIME_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (watchElement.getVersion() == NOT_EXIST_VERSION) {
            watchElement.setVersion(EXIST_MIN_VERSION);
        }

        invoke(EntityClassSyncProtoBufMocker.Response.entityClassSyncResponseGenerator(watchElement.getAppId(), watchElement.getVersion(),
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(System.currentTimeMillis())), null);
        return true;
    }

    @Override
    public boolean reRegister() {
        return false;
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
    public MetricsRecorder metricsRecorder() {
        return null;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void unused) {
        syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(),
            entityClassSyncResponse.getEntityClassSyncRspProto());
    }

    @Override
    public boolean isShutDown() {
        return false;
    }
}
