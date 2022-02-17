package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import io.grpc.stub.StreamObserver;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :
 * name : EntityClassSyncServerTest.
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class EntityClassSyncServerTest extends BaseInit {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncServerTest.class);

    private MockerSyncClient mockerSyncClient;

    private String clientId = "entityClassSyncServerTest";

    private ExecutorService fixed = Executors.newFixedThreadPool(1);

    static int port = 8999;

    @BeforeEach
    public void before() throws InterruptedException {
        String host = "localhost";
        initServer(port);
        Thread.sleep(5_000);
        mockerSyncClient = initClient(host, port);
    }

    @AfterEach
    public void after() throws InterruptedException {
        mockerSyncClient.stop();
        Thread.sleep(5_000);
        stopServer();
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

        observer.onNext(
            buildRequest(new WatchElement(appId, env, version, null), clientId, uid, RequestStatus.REGISTER));

        /**
         * 检查结果，最大3秒
         */
        waitForResult(8, expectedVersion, appId);

        /**
         * 当前版本小于元数据版本，将进入
         */
        expectedVersion = version + 3;
        entityId = Long.MAX_VALUE - 1000;
        entityClassGenerator.reset(expectedVersion, entityId);
        observer.onNext(
            buildRequest(new WatchElement(appId, env, version + 2, null), clientId, uid, RequestStatus.REGISTER));

        waitForResult(3, expectedVersion, appId);

        /**
         * 当前版本更新成功
         * check服务端当前watchElement的版本是否更新到当前版本
         * check appId关注列表是否存在当前uid
         * check 整体appId + env 对应的版本没有更新(这个版本更新只能由push产生)
         */
        int resetVersion = version + 4;
        observer.onNext(
            buildRequest(new WatchElement(appId, env, resetVersion, null), clientId, uid, RequestStatus.SYNC_OK));
        Thread.sleep(1_000);
        WatchElement element = responseWatchExecutor.watcher(uid).watches().get(appId);
        Assertions.assertEquals(element.getEnv(), env);
        Assertions.assertEquals(element.getVersion(), resetVersion);

        Set<String> appWatchers = responseWatchExecutor.appWatchers(appId, env);
        Assertions.assertTrue(appWatchers.contains(uid));
        Integer v = responseWatchExecutor.version(appId, env);
        Assertions.assertNull(v);
    }

    private void tryHeartBeat(String uid, String env, String appId, StreamObserver<EntityClassSyncRequest> observer) {
        try {
            EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                .setUid(uid)
                .setEnv(env)
                .setAppId(appId)
                .setStatus(RequestStatus.HEARTBEAT.ordinal())
                .build();

            observer.onNext(request);

            logger.debug("loops to send heartbeat, uid [{}]", uid);
        } catch (Exception e) {

        }
    }

    //@Test
    //public void failTest() throws InterruptedException {
    //    StreamObserver<EntityClassSyncRequest> observer = mockerSyncClient.responseEvent();
    //    String uid = UUID.randomUUID().toString();
    //    String appId = "syncFailTest";
    //    String env = "test";
    //    int version = 0;
    //
    //    int expectedVersion = version + 1;
    //    long entityId = Long.MAX_VALUE - 1000;
    //    entityClassGenerator.reset(expectedVersion, entityId);
    //    observer.onNext(buildRequest(new WatchElement(appId, env, expectedVersion, WatchElement.ElementStatus.Register), clientId, uid, RequestStatus.REGISTER));
    //
    //    /**
    //     * 当前版本更新失败
    //     * check服务端3秒内重新推一个新版本数据
    //     */
    //    try {
    //
    //        int resetVersion = expectedVersion + 1;
    //        entityClassGenerator.reset(resetVersion, entityId);
    //        syncResponseHandler.pull(uid, false, new WatchElement(appId, env, resetVersion, null), RequestStatus.SYNC);
    //
    //        Thread.sleep(3_000);
    //
    //        mockerSyncClient.releaseSuccess(appId);
    //        int currentWait = 0;
    //        while (true) {
    //            if (currentWait % 5 == 0) {
    //                tryHeartBeat(uid, env, appId, observer);
    //            }
    //            WatchElement element = mockerSyncClient.getSuccess(appId);
    //            if (null != element && element.getVersion() == resetVersion) {
    //                mockerSyncClient.releaseSuccess(appId);
    //                break;
    //            }
    //            currentWait++;
    //            Thread.sleep(1_000);
    //        }
    //        //  mock set sync fail
    //        observer.onNext(buildRequest(new WatchElement(appId, env, resetVersion, WatchElement.ElementStatus.Confirmed), clientId, uid, RequestStatus.SYNC_FAIL));
    //
    //        waitForResult(50, resetVersion, appId);
    //    } finally {
    //        ExecutorHelper.shutdownAndAwaitTermination(fixed, 10);
    //    }
    //}


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
        observer.onNext(
            buildRequest(new WatchElement(appId, env, version, WatchElement.ElementStatus.Register), clientId, uid,
                RequestStatus.REGISTER));

        Thread.sleep(2_000);

        /**
         * 发送心跳
         */
        long heartBeat = System.currentTimeMillis();
        observer.onNext(
            buildRequest(new WatchElement(appId, env, version, WatchElement.ElementStatus.Register), clientId, uid,
                RequestStatus.HEARTBEAT));


        Thread.sleep(2_000);
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);

        Assertions.assertNotNull(watcher);
        Assertions.assertTrue(watcher.heartBeat() > heartBeat);
    }

    private void waitResultAndHeartBeat(String uid, String env, int maxWaitLoops, int version,
                                        String appId, StreamObserver<EntityClassSyncRequest> observer)
        throws InterruptedException {
        int currentWait = 0;
        while (currentWait < maxWaitLoops) {
            if (currentWait % 5 == 0) {
                tryHeartBeat(uid, env, appId, observer);
            }
            WatchElement w = mockerSyncClient.getSuccess(appId);
            if (null != w && version == w.getVersion()) {
                logger.info("check version ok, waitForResult will be fin, appId-{}, version-{}, waits-{}", appId,
                    version, currentWait);
                break;
            }
            currentWait++;
            Thread.sleep(1_000);
        }

        Assertions.assertNotEquals(currentWait, maxWaitLoops);

        mockerSyncClient.releaseSuccess(appId);
    }

    private void waitForResult(int maxWaitLoops, int version, String appId) throws InterruptedException {
        int currentWait = 0;
        while (currentWait < maxWaitLoops) {
            WatchElement w = mockerSyncClient.getSuccess(appId);
            if (null != w && version == w.getVersion()) {
                logger.info("check version ok, waitForResult will be fin, appId-{}, version-{}, waits-{}", appId,
                    version, currentWait);
                break;
            }
            currentWait++;
            Thread.sleep(1_000);
        }

        Assertions.assertNotEquals(currentWait, maxWaitLoops);

        mockerSyncClient.releaseSuccess(appId);
    }


}
