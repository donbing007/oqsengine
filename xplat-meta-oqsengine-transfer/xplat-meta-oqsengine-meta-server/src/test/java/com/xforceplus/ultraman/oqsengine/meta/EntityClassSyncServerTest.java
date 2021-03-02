package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockClient;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
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
    public void before() throws InterruptedException {

        String host = "localhost";
        int port = 8899;

        entityClassSyncServer = entityClassSyncServer();

        gRpcExecutor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcServer = new GRpcServer(port);
        ReflectionTestUtils.setField(gRpcServer, "entityClassSyncServer", entityClassSyncServer);
        ReflectionTestUtils.setField(gRpcServer, "configuration", gRpcParamsConfig);
        ReflectionTestUtils.setField(gRpcServer, "gRpcExecutor", gRpcExecutor);

        gRpcServer.start();

        Thread.sleep(1_000);

        //  start client
        MockClient mockClient = new MockClient();

        mockerSyncClient = new MockerSyncClient();
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
    public void heartBeatTest() throws InterruptedException {
        StreamObserver<EntityClassSyncRequest> observer = mockerSyncClient.responseEvent();

        String uid = UUID.randomUUID().toString();
        String appId = "heartBeatTest";
        String env = "test";
        int version = 1;

        /**
         * 注册
         */
        observer.onNext(buildRequest(uid, appId, version, RequestStatus.REGISTER.ordinal(), env));

        /**
         * 发送心跳
         */
        observer.onNext(buildRequest(uid, appId, version, RequestStatus.HEARTBEAT.ordinal(), env));


        Thread.sleep(1_000);
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        Assert.assertNotNull(watcher);

        WatchElement watchElement = watcher.watches().get(appId);

        Assert.assertEquals(env, watchElement.getEnv());
        Assert.assertEquals(version, watchElement.getVersion());

        /**
         * 当超出最大心跳时间时，将移除该watcher
         */
        Thread.sleep(31_000);
        watcher = responseWatchExecutor.watcher(uid);
        Assert.assertNull(watcher);
    }

    @Test
    public void registerSyncTest() throws InterruptedException {
        StreamObserver<EntityClassSyncRequest> observer = mockerSyncClient.responseEvent();

        String uid = UUID.randomUUID().toString();
        String appId = "registerTest";
        String env = "test";
        int version = 0;

        int expectedVersion = version + 1;
        long entityId = Long.MAX_VALUE - 100;
        entityClassGenerator.reset(expectedVersion, entityId);

        observer.onNext(buildRequest(uid, appId, version, RequestStatus.REGISTER.ordinal(), env));

        /**
         * 检查结果，最大3秒
         */
        waitForResult(3, expectedVersion);

        /**
         * 当前版本小于元数据版本，将进入
         */
        expectedVersion = version + 3;
        entityId = Long.MAX_VALUE - 1000;
        entityClassGenerator.reset(expectedVersion, entityId);
        observer.onNext(buildRequest(uid, appId, version + 2, RequestStatus.REGISTER.ordinal(), env));

        waitForResult(3, expectedVersion);

        /**
         * 当前版本更新成功
         * check服务端当前watchElement的版本是否更新到当前版本
         * check appId关注列表是否存在当前uid
         * check 整体appId + env 对应的版本没有更新(这个版本更新只能由push产生)
         */
        int resetVersion = version + 4;
        observer.onNext(buildRequest(uid, appId, resetVersion, RequestStatus.SYNC_OK.ordinal(), env));
        Thread.sleep(1_000);
        WatchElement element = responseWatchExecutor.watcher(uid).watches().get(appId);
        Assert.assertEquals(element.getEnv(), env);
        Assert.assertEquals(element.getVersion(), resetVersion);

        Set<String> appWatchers = responseWatchExecutor.appWatchers(appId, env);
        Assert.assertTrue(appWatchers.contains(uid));
        Integer v = responseWatchExecutor.version(appId, env);
        Assert.assertNull(v);

        /**
         * 当前版本更新失败
         * check服务端3秒内重新推一个新版本数据
         */
        resetVersion = version + 5;
        entityClassGenerator.reset(resetVersion, entityId);
        observer.onNext(buildRequest(uid, appId, resetVersion, RequestStatus.SYNC_FAIL.ordinal(), env));
        waitForResult(3, resetVersion);


    }

    private void waitForResult(int maxWaitLoops, int version) throws InterruptedException {
        int currentWait = 0;
        while (currentWait < maxWaitLoops) {
            if (null == mockerSyncClient.getSuccess()) {
                Thread.sleep(1_000);
            }
            currentWait++;
        }
        WatchElement w = mockerSyncClient.getSuccess();

        Assert.assertNotNull(w);
        Assert.assertEquals(version, w.getVersion());

        mockerSyncClient.releaseSuccess();
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
