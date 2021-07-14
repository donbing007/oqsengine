package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.BaseTest;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.*;


/**
 * desc :
 * name : RequestWatchExecutorTest
 *
 * @author : xujia
 * date : 2021/2/24
 * @since : 1.8
 */
public class RequestWatchExecutorTest extends BaseTest {


    @BeforeEach
    public void before() {
        requestWatchExecutor = requestWatchExecutor();
        RequestWatcher requestWatcher = new RequestWatcher(testClientId, UUID.randomUUID().toString(), mockObserver());
        ReflectionTestUtils.setField(requestWatchExecutor, "requestWatcher", requestWatcher);

        requestWatchExecutor.start();
    }

    @AfterEach
    public void after() {
        requestWatchExecutor.stop();
    }

    @Test
    public void resetHeartBeatTest() throws InterruptedException {
        Assertions.assertNotNull(requestWatchExecutor.watcher());
        long heartbeat = requestWatchExecutor.watcher().heartBeat();
        Thread.sleep(1);
        requestWatchExecutor.resetHeartBeat("uid");
        Assertions.assertNotEquals(heartbeat, requestWatchExecutor.watcher().heartBeat());
    }

    @Test
    public void createTest() throws InterruptedException {
        Assertions.assertNotNull(requestWatchExecutor.watcher());
        String uid = requestWatchExecutor.watcher().uid();
        long heartbeat = requestWatchExecutor.watcher().heartBeat();

        Thread.sleep(1);
        StreamObserver<EntityClassSyncRequest> observer = requestWatchExecutor.watcher().observer();
        requestWatchExecutor.create(testClientId, UUID.randomUUID().toString(), mockObserver());
        Assertions.assertNotEquals(uid, requestWatchExecutor.watcher().uid());
        Assertions.assertNotEquals(heartbeat, requestWatchExecutor.watcher().heartBeat());
        Assertions.assertNotEquals(observer, requestWatchExecutor.watcher().observer());
    }

    @Test
    public void addTest() {
        String appId = "testAdd";
        String env = "test";
        int version = 12345;
        WatchElement w = new WatchElement(appId, env, version, Init);

        requestWatchExecutor.add(w);

        Assertions.assertEquals(1, requestWatchExecutor.watcher().watches().size());

        /**
         * 重复添加
         */
        requestWatchExecutor.add(w);
        Assertions.assertEquals(1, requestWatchExecutor.watcher().watches().size());
    }

    @Test
    public void updateTest() {
        String appId = "testAdd";
        String env = "test";
        int version = 10;
        WatchElement w = new WatchElement(appId, env, version, Init);

        requestWatchExecutor.add(w);

        /**
         * 设置一个小于当前的版本,将被拒绝
         */
        w = new WatchElement(appId, env, 9, Confirmed);
        boolean ret = requestWatchExecutor.update(w);
        Assertions.assertFalse(ret);

        /**
         * 设置当前版本 10 -> 10, init -> register,将被接收
         */
        w = new WatchElement(appId, env, 10, Register);
        ret = requestWatchExecutor.update(w);
        Assertions.assertTrue(ret);

        /**
         * 设置当前版本 10 -> 10, register -> init,将被拒绝
         */
        w = new WatchElement(appId, env,10, Init);
        ret = requestWatchExecutor.update(w);
        Assertions.assertFalse(ret);

        /**
         * 设置当前版本 10 -> 10, register -> confirm,将被接收
         */
        w = new WatchElement(appId, env,10, Confirmed);
        ret = requestWatchExecutor.update(w);
        Assertions.assertTrue(ret);
    }

    @Test
    public void isAliveTest() {
        String expectedId = requestWatchExecutor.watcher().uid();
        /**
         * on server, uid = expectedId
         * true
         */
        boolean ret = requestWatchExecutor.isAlive(expectedId);
        Assertions.assertTrue(ret);

        /**
         * on server, uid = new Id
         * false
         */
        String uid = UUID.randomUUID().toString();
        ret = requestWatchExecutor.isAlive(uid);
        Assertions.assertFalse(ret);

        /**
         * off server, uid = expectedId
         * false
         */
        requestWatchExecutor.inActive();
        ret = requestWatchExecutor.isAlive(expectedId);
        Assertions.assertFalse(ret);

        /**
         * off server, uid = new Id
         * false
         */
        ret = requestWatchExecutor.isAlive(uid);
        Assertions.assertFalse(ret);
    }


    private StreamObserver<EntityClassSyncRequest> mockObserver() {
        return new StreamObserver<EntityClassSyncRequest>() {
            @Override
            public void onNext(EntityClassSyncRequest entityClassSyncRequest) {
                //  do nothing
            }

            @Override
            public void onError(Throwable throwable) {
                //  do nothing
            }

            @Override
            public void onCompleted() {
                //  do nothing
            }
        };
    }
}
