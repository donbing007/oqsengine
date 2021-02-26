package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;


/**
 * desc :
 * name : RequestWatchExecutorTest
 *
 * @author : xujia
 * date : 2021/2/24
 * @since : 1.8
 */
public class RequestWatchExecutorTest {

    private RequestWatchExecutor requestWatchExecutor;

    private GRpcParamsConfig gRpcParamsConfig;

    @Before
    public void before() {
        gRpcParamsConfig = gRpcParamsConfig();
        requestWatchExecutor = requestWatchExecutor();
    }

    @After
    public void after() {
        requestWatchExecutor.stop();
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

    private RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        RequestWatcher requestWatcher = new RequestWatcher(UUID.randomUUID().toString(), mockObserver());
        ReflectionTestUtils.setField(requestWatchExecutor, "gRpcParamsConfig", gRpcParamsConfig);
        ReflectionTestUtils.setField(requestWatchExecutor, "requestWatcher", requestWatcher);
        return requestWatchExecutor;
    }

    @Test
    public void resetHeartBeatTest() {
        Assert.assertNotNull(requestWatchExecutor.watcher());
        long heartbeat = requestWatchExecutor.watcher().heartBeat();
        requestWatchExecutor.resetHeartBeat("uid");
        Assert.assertNotEquals(heartbeat, requestWatchExecutor.watcher().heartBeat());
    }

    @Test
    public void createTest() {
        Assert.assertNotNull(requestWatchExecutor.watcher());
        String uid = requestWatchExecutor.watcher().uid();
        long heartbeat = requestWatchExecutor.watcher().heartBeat();
        StreamObserver<EntityClassSyncRequest> observer = requestWatchExecutor.watcher().observer();

        requestWatchExecutor.create(UUID.randomUUID().toString(), mockObserver());
        Assert.assertNotEquals(uid, requestWatchExecutor.watcher().uid());
        Assert.assertNotEquals(heartbeat, requestWatchExecutor.watcher().heartBeat());
        Assert.assertNotEquals(observer, requestWatchExecutor.watcher().observer());
    }

    @Test
    public void addTest() {
        String appId = "testAdd";
        String env = "test";
        int version = 12345;
        WatchElement w = new WatchElement(appId, env, version, WatchElement.AppStatus.Init);

        requestWatchExecutor.add(w);

        Assert.assertEquals(1, requestWatchExecutor.watcher().watches().size());

        /**
         * 重复添加
         */
        requestWatchExecutor.add(w);
        Assert.assertEquals(1, requestWatchExecutor.watcher().watches().size());
    }

    @Test
    public void updateTest() {
        String appId = "testAdd";
        String env = "test";
        int version = 10;
        WatchElement w = new WatchElement(appId, env, version, WatchElement.AppStatus.Init);

        requestWatchExecutor.add(w);

        /**
         * 设置一个小于当前的版本,将被拒绝
         */
        w = new WatchElement(appId, env, 9, WatchElement.AppStatus.Confirmed);
        boolean ret = requestWatchExecutor.update(w);
        Assert.assertFalse(ret);

        /**
         * 设置当前版本 10 -> 10, init -> register,将被接收
         */
        w = new WatchElement(appId, env, 10, WatchElement.AppStatus.Register);
        ret = requestWatchExecutor.update(w);
        Assert.assertTrue(ret);

        /**
         * 设置当前版本 10 -> 10, register -> init,将被拒绝
         */
        w = new WatchElement(appId, env,10, WatchElement.AppStatus.Init);
        ret = requestWatchExecutor.update(w);
        Assert.assertFalse(ret);

        /**
         * 设置当前版本 10 -> 10, register -> confirm,将被接收
         */
        w = new WatchElement(appId, env,10, WatchElement.AppStatus.Confirmed);
        ret = requestWatchExecutor.update(w);
        Assert.assertTrue(ret);
    }

    @Test
    public void canAccessTest() {
        String expectedId = requestWatchExecutor.watcher().uid();
        /**
         * on server, uid = expectedId
         * true
         */
        boolean ret = requestWatchExecutor.canAccess(expectedId);
        Assert.assertTrue(ret);

        /**
         * on server, uid = new Id
         * false
         */
        String uid = UUID.randomUUID().toString();
        ret = requestWatchExecutor.canAccess(uid);
        Assert.assertFalse(ret);

        /**
         * off server, uid = expectedId
         * false
         */
        requestWatchExecutor.watcher().notServer();
        ret = requestWatchExecutor.canAccess(expectedId);
        Assert.assertFalse(ret);

        /**
         * off server, uid = new Id
         * false
         */
        ret = requestWatchExecutor.canAccess(uid);
        Assert.assertFalse(ret);
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
