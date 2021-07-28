package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;
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

    private final Logger logger = LoggerFactory.getLogger(SyncResponseHandlerTest.class);

    private ResponseWatchExecutor responseWatchExecutor;
    private IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;
    private MockEntityClassGenerator entityClassGenerator;
    private ExecutorService executor;
    private GRpcParams gRpcParamsConfig;

    private SyncResponseHandler syncResponseHandler;

    @BeforeEach
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
        ReflectionTestUtils.setField(syncResponseHandler, "grpcParams", gRpcParamsConfig);

        syncResponseHandler.start();
    }

    @AfterEach
    public void after() {
        syncResponseHandler.stop();
        ExecutorHelper.shutdownAndAwaitTermination(executor, 10);
    }

    private GRpcParams gRpcParamsConfig() {
        GRpcParams gRpcParamsConfig = new GRpcParams();
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
                    syncResponseHandler.invoke(entityClassSyncRequest(tCase), tCase.getStreamObserver());

                    /**
                     * register
                     */
                    WatchElement w =
                            new WatchElement(tCase.getAppId(), tCase.getEnv(),
                                    tCase.getVersion() + 1, Register);

                    Assertions.assertTrue(responseWatchExecutor.watcher(tCase.getUid()).onWatch(w));
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
                    syncResponseHandler.invoke(entityClassSyncRequest(tCase), tCase.getStreamObserver());

                    /**
                     * register
                     */
                    WatchElement w =
                            new WatchElement(tCase.getAppId(), tCase.getEnv(),
                                    tCase.getVersion() + 1, Register);

                    Assertions.assertTrue(responseWatchExecutor.watcher(tCase.getUid()).onWatch(w));
                }
        );

        int count = 0;
        testCase.get(0).resetStatus(HEARTBEAT.ordinal());
        EntityClassSyncRequest entityClassSyncRequest = entityClassSyncRequest(testCase.get(0));
        while(count < 35) {
            syncResponseHandler.invoke(entityClassSyncRequest, testCase.get(0).getStreamObserver());
            Thread.sleep(1_000);
            count ++;
        }

        Assertions.assertTrue(responseWatchExecutor.watcher(testCase.get(0).getUid()).isActive());

        Assertions.assertNull(responseWatchExecutor.watcher(testCase.get(1).getUid()));
    }

    @Test
    public void onNextSyncOkTest() throws InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver();

        Case t = new Case(uid1, "appId1", "test", 1, REGISTER.ordinal(), responseStreamObserver1);
        syncResponseHandler.invoke(entityClassSyncRequest(t), t.getStreamObserver());
        Thread.sleep(1_000);
        Case t2 = new Case(uid1, "appId1", "test", 2, SYNC_OK.ordinal(), responseStreamObserver1);

        WatchElement w = responseWatchExecutor.watcher(uid1).watches().get(t.getAppId());
        Assertions.assertNotEquals(Confirmed, w.getStatus());

        syncResponseHandler.invoke(entityClassSyncRequest(t2), t2.getStreamObserver());

        w = responseWatchExecutor.watcher(uid1).watches().get(t.getAppId());
        Assertions.assertNotNull(w);
        Assertions.assertEquals(Confirmed, w.getStatus());
    }

    @Test
    public void onNextSyncFailTest() throws InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        String appId = "appId1";
        String env = "test";
        int failedVersion = 2;
        StreamObserver<EntityClassSyncResponse> responseStreamObserver1 = newStreamObserver(uid1, appId, env, failedVersion);

        Case t = new Case(uid1, appId, env, 1, REGISTER.ordinal(), responseStreamObserver1);
        syncResponseHandler.invoke(entityClassSyncRequest(t), t.getStreamObserver());
        Thread.sleep(1_000);
        Case t2 = new Case(uid1, "appId1", "test", failedVersion, SYNC_FAIL.ordinal(), responseStreamObserver1);

        entityClassGenerator.reset(failedVersion, System.currentTimeMillis());
        syncResponseHandler.invoke(entityClassSyncRequest(t2), t2.getStreamObserver());

        ResponseWatcher watcher = responseWatchExecutor.watcher(uid1);
        Assertions.assertNotNull(watcher);
        WatchElement w = watcher.watches().get(t.getAppId());
        Assertions.assertNotNull(w);
        Assertions.assertEquals(Register, w.getStatus());

        Thread.sleep(5_000);

    }

    private StreamObserver<EntityClassSyncResponse> newStreamObserver(String uid, String appId, String env, int version) {
        return new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                if (entityClassSyncResponse.getStatus() == SYNC.ordinal()) {
                    Assertions.assertEquals(uid, entityClassSyncResponse.getUid());
                    Assertions.assertEquals(appId, entityClassSyncResponse.getAppId());
                    Assertions.assertEquals(env, entityClassSyncResponse.getEnv());
                    Assertions.assertEquals(version, entityClassSyncResponse.getVersion());

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
        private final String uid;
        private final String appId;
        private final String env;
        private final int version;
        private int status;

        private final StreamObserver<EntityClassSyncResponse> streamObserver;

        public Case(String uid, String appId, String env, int version, int status,
                    StreamObserver<EntityClassSyncResponse> streamObserver) {
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
