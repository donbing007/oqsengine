package com.xforceplus.ultraman.oqsengine.metadata.remote;


import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : RemoteBaseRequest
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class RemoteBaseRequest {

    protected IRequestHandler requestHandler;

    protected GRpcParams grpcParams;

    protected RequestWatchExecutor requestWatchExecutor;

    protected ExecutorService executorService;

    protected EntityClassSyncClient entityClassSyncClient;

    protected void baseInit(SyncExecutor syncExecutor) {
        grpcParams = RemoteConstant.grpcParamsConfig();

        requestWatchExecutor = requestWatchExecutor();

        this.requestHandler = requestHandler(syncExecutor);

        entityClassSyncClient = entityClassSyncClient();

    }

    protected IRequestHandler requestHandler(SyncExecutor syncExecutor) {
        IRequestHandler requestHandler = new SyncRequestHandler();
        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);
        ReflectionTestUtils.setField(requestHandler, "gRpcParams", grpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }

    protected EntityClassSyncClient entityClassSyncClient() {

        MetaSyncGRpcClient metaSyncGrpcClient = new MetaSyncGRpcClient(RemoteConstant.HOST, RemoteConstant.PORT);
        ReflectionTestUtils.setField(metaSyncGrpcClient, "gRpcParams", grpcParams);

        EntityClassSyncClient entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", metaSyncGrpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "gRpcParamsConfig", grpcParams);

        return entityClassSyncClient;
    }

    protected RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
    }


}
