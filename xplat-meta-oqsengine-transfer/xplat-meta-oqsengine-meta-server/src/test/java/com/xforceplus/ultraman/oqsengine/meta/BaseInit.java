package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockClient;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : BaseInit
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class BaseInit {
    protected EntityClassSyncServer entityClassSyncServer;

    protected ResponseWatchExecutor responseWatchExecutor;
    protected IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;
    protected MockEntityClassGenerator entityClassGenerator;

    protected GRpcParamsConfig gRpcParamsConfig;

    protected SyncResponseHandler syncResponseHandler;

    protected ExecutorService taskExecutor;

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
        syncResponseHandler = syncResponseHandler();
        EntityClassSyncServer syncServer = new EntityClassSyncServer();
        ReflectionTestUtils.setField(syncServer, "responseHandler", syncResponseHandler);
        return syncServer;
    }

    protected SyncResponseHandler syncResponseHandler() {
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

    protected GRpcParamsConfig gRpcParamsConfig() {
        GRpcParamsConfig gRpcParamsConfig = new GRpcParamsConfig();
        gRpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        gRpcParamsConfig.setKeepAliveSendDuration(5_000);
        gRpcParamsConfig.setReconnectDuration(5_000);
        gRpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        gRpcParamsConfig.setMonitorSleepDuration(1_000);

        return gRpcParamsConfig;
    }

    protected MockerSyncClient initClient(String host, int port) {
        //  start client
        MockClient mockClient = new MockClient();

        MockerSyncClient mockerSyncClient = new MockerSyncClient();
        ReflectionTestUtils.setField(mockerSyncClient, "mockClient", mockClient);
        mockerSyncClient.start(host, port);

        return mockerSyncClient;
    }
}
