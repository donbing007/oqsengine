package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockClient;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : EntityClassSyncServerTest
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class EntityClassSyncServerTest {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncServerTest.class);

    private GRpcServer gRpcServer;

    private MockerSyncClient mockerSyncClient;

    private EntityClassSyncServer entityClassSyncServer;

    private ResponseWatchExecutor responseWatchExecutor;
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;
    private MockEntityClassGenerator entityClassGenerator;

    private GRpcParamsConfig gRpcParamsConfig;

    private SyncResponseHandler syncResponseHandler;

    private ExecutorService taskExecutor;

    private ExecutorService gRpcExecutor;

    @Before
    public void before() {

        String host = "localhost";
        int port = 9999;

        entityClassSyncServer = entityClassSyncServer();

        gRpcExecutor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcServer = new GRpcServer(port);
        ReflectionTestUtils.setField(gRpcServer, "entityClassSyncServer", entityClassSyncServer);
        ReflectionTestUtils.setField(gRpcServer, "configuration", gRpcParamsConfig);
        ReflectionTestUtils.setField(gRpcServer, "gRpcExecutor", gRpcExecutor);

        gRpcServer.start();

        //  start client
        MockClient mockClient = new MockClient();
        ReflectionTestUtils.setField(mockerSyncClient, "mockClient", mockClient);
        mockerSyncClient.start(host, port);
    }

    @After
    public void after() {
        gRpcServer.stop();
        mockerSyncClient.stop();
        ExecutorHelper.shutdownAndAwaitTermination(taskExecutor);
        ExecutorHelper.shutdownAndAwaitTermination(gRpcExecutor);
    }

    private EntityClassSyncServer entityClassSyncServer() {
        syncResponseHandler = syncResponseHandler();
        EntityClassSyncServer syncServer = new EntityClassSyncServer();
        ReflectionTestUtils.setField(syncServer, "responseHandler", syncResponseHandler);
        return syncServer;
    }

    private SyncResponseHandler syncResponseHandler() {
        responseWatchExecutor = new ResponseWatchExecutor();
        retryExecutor = new RetryExecutor();

        entityClassGenerator = new MockEntityClassGenerator();

        taskExecutor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcParamsConfig = gRpcParamsConfig();

        SyncResponseHandler syncResponseHandler = new SyncResponseHandler();
        ReflectionTestUtils.setField(syncResponseHandler, "responseWatchExecutor", responseWatchExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "retryExecutor", retryExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "entityClassGenerator", entityClassGenerator);
        ReflectionTestUtils.setField(syncResponseHandler, "taskExecutor", taskExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "gRpcParamsConfig", gRpcParamsConfig);

        return syncResponseHandler;
    }

    private GRpcParamsConfig gRpcParamsConfig() {
        GRpcParamsConfig gRpcParamsConfig = new GRpcParamsConfig();
        gRpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        gRpcParamsConfig.setKeepAliveSendDuration(5_000);
        gRpcParamsConfig.setReconnectDuration(5_000);
        gRpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        gRpcParamsConfig.setMonitorSleepDuration(1_000);

        return gRpcParamsConfig;
    }

    @Test
    public void heartBeatTest() {
        StreamObserver<EntityClassSyncRequest> observer = mockerSyncClient.responseEvent();

        String uid = UUID.randomUUID().toString();
        String appId = "heartBeatTest";
        String env = "test";
        int version = 1;
        int status = RequestStatus.HEARTBEAT.ordinal();

        observer.onNext(buildRequest(uid, appId, version, status, env));


    }



    private EntityClassSyncRequest buildRequest(String uid, String appId, int version, int status, String env) {
        return EntityClassSyncRequest.newBuilder()
                .setUid(uid)
                .setAppId(appId)
                .setVersion(version)
                .setStatus(status)
                .setEnv(env)
                .build();
    }

}
