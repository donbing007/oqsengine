package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import java.util.concurrent.ExecutorService;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : BaseRequest
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class MockerRequestClientHelper extends AbstractMetaTestHelper {

    protected IRequestHandler requestHandler;

    protected GRpcParams grpcParams;

    protected RequestWatchExecutor requestWatchExecutor;

    protected ExecutorService executorService;

    protected EntityClassSyncClient entityClassSyncClient;

    protected void init(boolean isLocal) throws IllegalAccessException {
        grpcParams = Constant.grpcParamsConfig();

        requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler();

        entityClassSyncClient = entityClassSyncClient(isLocal);

        entityClassSyncClient.start();

        super.init(requestHandler);
    }

    protected void destroy() throws Exception {
        entityClassSyncClient.stop();
        super.destroy();
    }

    protected EntityClassSyncClient entityClassSyncClient(boolean isLocal) {
        MetaSyncGRpcClient metaSyncGrpcClient;
        if (isLocal) {
            metaSyncGrpcClient = new MetaSyncGRpcClient(Constant.LOCAL_HOST, Constant.LOCAL_PORT);
        } else {
            metaSyncGrpcClient = new MetaSyncGRpcClient(Constant.REMOTE_HOST, Constant.REMOTE_PORT);
        }

        ReflectionTestUtils.setField(metaSyncGrpcClient, "grpcParams", grpcParams);

        EntityClassSyncClient entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", metaSyncGrpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "grpcParamsConfig", grpcParams);

        return entityClassSyncClient;
    }

    protected RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
    }

    protected IRequestHandler requestHandler() throws IllegalAccessException {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = MetaInitialization.getInstance().getEntityClassSyncExecutor();

        executorService = CommonInitialization.getInstance().getRunner();

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);
        ReflectionTestUtils.setField(requestHandler, "grpcParams", grpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }
}
