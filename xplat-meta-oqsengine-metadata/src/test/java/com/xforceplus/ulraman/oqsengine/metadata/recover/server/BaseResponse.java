package com.xforceplus.ulraman.oqsengine.metadata.recover.server;

import com.xforceplus.ulraman.oqsengine.metadata.recover.server.mock.MockEntityClassGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ulraman.oqsengine.metadata.recover.Constant.gRpcParamsConfig;

/**
 * desc :
 * name : BaseResponse
 *
 * @author : xujia
 * date : 2021/4/7
 * @since : 1.8
 */
public class BaseResponse {
    protected EntityClassSyncServer entityClassSyncServer;

    protected ResponseWatchExecutor responseWatchExecutor;

    protected IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;

    protected GRpcParams gRpcParamsConfig;

    protected SyncResponseHandler syncResponseHandler;

    protected ExecutorService taskExecutor;

    protected MockEntityClassGenerator mockEntityClassGenerator;

    private GRpcServer gRpcServer;
    private ExecutorService gRpcExecutor;


    protected void stopServer() {
        gRpcServer.stop();
        ExecutorHelper.shutdownAndAwaitTermination(taskExecutor);
        ExecutorHelper.shutdownAndAwaitTermination(gRpcExecutor);
    }

    protected void initServer(int port) throws InterruptedException {
        entityClassSyncServer = entityClassSyncServer();

        gRpcExecutor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcServer = new GRpcServer(port);
        ReflectionTestUtils.setField(gRpcServer, "entityClassSyncServer", entityClassSyncServer);
        ReflectionTestUtils.setField(gRpcServer, "configuration", gRpcParamsConfig);
        ReflectionTestUtils.setField(gRpcServer, "gRpcExecutor", gRpcExecutor);

        gRpcServer.start();
    }

    protected EntityClassSyncServer entityClassSyncServer() {

        mockEntityClassGenerator = new MockEntityClassGenerator();
        syncResponseHandler = syncResponseHandler();
        EntityClassSyncServer syncServer = new EntityClassSyncServer();
        ReflectionTestUtils.setField(syncServer, "responseHandler", syncResponseHandler);
        return syncServer;
    }

    protected SyncResponseHandler syncResponseHandler() {
        responseWatchExecutor = new ResponseWatchExecutor();
        retryExecutor = new RetryExecutor();

        taskExecutor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcParamsConfig = gRpcParamsConfig();

        SyncResponseHandler syncResponseHandler = new SyncResponseHandler();
        ReflectionTestUtils.setField(syncResponseHandler, "responseWatchExecutor", responseWatchExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "retryExecutor", retryExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "entityClassGenerator", mockEntityClassGenerator);
        ReflectionTestUtils.setField(syncResponseHandler, "taskExecutor", taskExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "gRpcParams", gRpcParamsConfig);

        return syncResponseHandler;
    }


    protected EntityClassSyncRequest buildRequest(WatchElement w, String uid, RequestStatus requestStatus) {
        return EntityClassSyncRequest.newBuilder()
                .setUid(uid)
                .setAppId(w.getAppId())
                .setVersion(w.getVersion())
                .setStatus(requestStatus.ordinal())
                .setEnv(w.getEnv())
                .build();
    }
}
