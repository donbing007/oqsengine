package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.AppStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.AppStatus.Register;
import static com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassSyncRequestBuilder.entityClassSyncRequest;


/**
 * desc :
 * name : SyncResponseHandlerTest
 *
 * @author : xujia
 * date : 2021/3/1
 * @since : 1.8
 */
public class SyncResponseHandlerTest {

    private Logger logger = LoggerFactory.getLogger(SyncResponseHandlerTest.class);

    private ResponseWatchExecutor responseWatchExecutor;
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;
    private MockEntityClassGenerator entityClassGenerator;
    private ExecutorService executor;
    private GRpcParamsConfig gRpcParamsConfig;

    private SyncResponseHandler syncResponseHandler;

    @Before
    public void before() {

        responseWatchExecutor = new ResponseWatchExecutor();
        retryExecutor = new RetryExecutor();

        entityClassGenerator = new MockEntityClassGenerator();

        executor = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        gRpcParamsConfig = gRpcParamsConfig();

        syncResponseHandler = new SyncResponseHandler();
        ReflectionTestUtils.setField(syncResponseHandler, "responseWatchExecutor", responseWatchExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "retryExecutor", retryExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "entityClassGenerator", entityClassGenerator);
        ReflectionTestUtils.setField(syncResponseHandler, "taskExecutor", executor);
        ReflectionTestUtils.setField(syncResponseHandler, "gRpcParamsConfig", gRpcParamsConfig);

        syncResponseHandler.start();
    }

    @After
    public void after() {
        syncResponseHandler.stop();
        ExecutorHelper.shutdownAndAwaitTermination(executor);
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
    public void onNextRegisterTest() {
        List<Case> testCase = new ArrayList<>();

        String uid1 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver();
        testCase.add(new Case(uid1, "appId1", "test", -1, REGISTER.ordinal(), responseStreamObserver1));
        testCase.add(new Case(uid1, "appId2", "test", -1, REGISTER.ordinal(), responseStreamObserver1));

        String uid2 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver2 = newStreamObserver();
        testCase.add(new Case(uid2, "appId1", "test", -1, REGISTER.ordinal(), responseStreamObserver2));
        testCase.add(new Case(uid2, "appId2", "test", -1, REGISTER.ordinal(), responseStreamObserver2));

        testCase.forEach(
                tCase -> {
                    syncResponseHandler.onNext(entityClassSyncRequest(tCase), tCase.getStreamObserver());

                    /**
                     * register
                     */
                    WatchElement w =
                            new WatchElement(tCase.getAppId(), tCase.getEnv(),
                                    tCase.getVersion() + 1, Register);

                    Assert.assertTrue(responseWatchExecutor.watcher(tCase.getUid()).onWatch(w));
                }
        );
    }

    @Test
    public void onNextHeartBeatTest() throws InterruptedException {
        List<Case> testCase = new ArrayList<>();
        String uid1 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver();
        testCase.add(new Case(uid1, "appId1", "test", -1, REGISTER.ordinal(), responseStreamObserver1));


        String uid2 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver2 = newStreamObserver();
        testCase.add(new Case(uid2, "appId1", "test", -1, REGISTER.ordinal(), responseStreamObserver2));

        testCase.forEach(
                tCase -> {
                    syncResponseHandler.onNext(entityClassSyncRequest(tCase), tCase.getStreamObserver());

                    /**
                     * register
                     */
                    WatchElement w =
                            new WatchElement(tCase.getAppId(), tCase.getEnv(),
                                    tCase.getVersion() + 1, Register);

                    Assert.assertTrue(responseWatchExecutor.watcher(tCase.getUid()).onWatch(w));
                }
        );

        int count = 0;
        testCase.get(0).resetStatus(HEARTBEAT.ordinal());
        EntityClassSyncRequest entityClassSyncRequest = entityClassSyncRequest(testCase.get(0));
        while(count < 35) {
            syncResponseHandler.onNext(entityClassSyncRequest, testCase.get(0).getStreamObserver());
            Thread.sleep(1_000);
            count ++;
        }

        Assert.assertTrue(responseWatchExecutor.watcher(testCase.get(0).getUid()).isOnServe());

        Assert.assertNull(responseWatchExecutor.watcher(testCase.get(1).getUid()));
    }

    @Test
    public void onNextSyncOkTest() throws InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver();

        Case t = new Case(uid1, "appId1", "test", 1, REGISTER.ordinal(), responseStreamObserver1);
        syncResponseHandler.onNext(entityClassSyncRequest(t), t.getStreamObserver());
        Thread.sleep(1_000);
        Case t2 = new Case(uid1, "appId1", "test", 2, SYNC_OK.ordinal(), responseStreamObserver1);

        WatchElement w = responseWatchExecutor.watcher(uid1).watches().get(t.getAppId());
        Assert.assertNotEquals(Confirmed, w.getStatus());

        syncResponseHandler.onNext(entityClassSyncRequest(t2), t2.getStreamObserver());

        w = responseWatchExecutor.watcher(uid1).watches().get(t.getAppId());
        Assert.assertNotNull(w);
        Assert.assertEquals(Confirmed, w.getStatus());
    }

    @Test
    public void onNextSyncFailTest() throws InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        String appId = "appId1";
        String env = "test";
        int failedVersion = 2;
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver(uid1, appId, env, failedVersion);

        Case t = new Case(uid1, appId, env, 1, REGISTER.ordinal(), responseStreamObserver1);
        syncResponseHandler.onNext(entityClassSyncRequest(t), t.getStreamObserver());
        Thread.sleep(1_000);
        Case t2 = new Case(uid1, "appId1", "test", failedVersion, SYNC_FAIL.ordinal(), responseStreamObserver1);

        entityClassGenerator.reset(failedVersion, System.currentTimeMillis());
        syncResponseHandler.onNext(entityClassSyncRequest(t2), t2.getStreamObserver());

        ResponseWatcher watcher = responseWatchExecutor.watcher(uid1);
        Assert.assertNotNull(watcher);
        WatchElement w = watcher.watches().get(t.getAppId());
        Assert.assertNotNull(w);
        Assert.assertEquals(Register, w.getStatus());

        Thread.sleep(5_000);

    }

    private StreamObserver<EntityClassSyncResponse> newStreamObserver(String uid, String appId, String env, int version) {
        return new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                if (entityClassSyncResponse.getStatus() == SYNC.ordinal()) {
                    Assert.assertEquals(uid, entityClassSyncResponse.getUid());
                    Assert.assertEquals(appId, entityClassSyncResponse.getAppId());
                    Assert.assertEquals(env, entityClassSyncResponse.getEnv());
                    Assert.assertEquals(version, entityClassSyncResponse.getVersion());

                    logger.info("client get sync message : uid [{}], appId [{}], env [{}], version [{}], response [{}]"
                                , entityClassSyncResponse.getUid()
                                , entityClassSyncResponse.getAppId()
                                , entityClassSyncResponse.getEnv()
                                , entityClassSyncResponse.getVersion()
                                , entityClassSyncResponse.getEntityClassSyncRspProto());
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    private StreamObserver<EntityClassSyncResponse> newStreamObserver() {
        return new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }


    public static class Case {
        private String uid;
        private String appId;
        private String env;
        private int version;
        private int status;

        private StreamObserver<EntityClassSyncResponse> streamObserver;

        public Case(String uid, String appId, String env, int version, int status, StreamObserver<EntityClassSyncResponse> streamObserver) {
            this.uid = uid;
            this.appId = appId;
            this.env = env;
            this.version = version;
            this.status = status;
            this.streamObserver = streamObserver;
        }

        public StreamObserver<EntityClassSyncResponse> getStreamObserver() {
            return streamObserver;
        }

        public String getUid() {
            return uid;
        }

        public String getAppId() {
            return appId;
        }

        public String getEnv() {
            return env;
        }

        public int getVersion() {
            return version;
        }

        public int getStatus() {
            return status;
        }


        public void resetStatus(int status) {
            this.status = status;
        }
    }

}
