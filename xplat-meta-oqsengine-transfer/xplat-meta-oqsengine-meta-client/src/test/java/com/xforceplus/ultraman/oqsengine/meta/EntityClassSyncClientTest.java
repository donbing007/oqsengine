package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.MockGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockServer;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : EntityClassSyncClientTest
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
public class EntityClassSyncClientTest {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncClientTest.class);

    private EntityClassSyncClient entityClassSyncClient;

    private MockGRpcClient mockGRpcClient;

    private IRequestHandler requestHandler;

    private GRpcParamsConfig gRpcParamsConfig;

    private RequestWatchExecutor requestWatchExecutor;

    @Before
    public void before() throws InterruptedException {

        mockGRpcClient = new MockGRpcClient();
        gRpcParamsConfig = gRpcParamsConfig();
        requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler();
        entityClassSyncClient = new EntityClassSyncClient();

        ExecutorService executorService = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        ReflectionTestUtils.setField(entityClassSyncClient, "client", mockGRpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "gRpcParamsConfig", gRpcParamsConfig);
        ReflectionTestUtils.setField(entityClassSyncClient, "executorService", executorService);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestWatchExecutor", requestWatchExecutor);

        entityClassSyncClient.start();

        Thread.sleep(5_000);
    }

    @After
    public void after() {
        entityClassSyncClient.destroy();
    }

    private IRequestHandler requestHandler() {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = new SyncExecutor() {
            Map<String, Integer> stringIntegerMap = new HashMap<>();

            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                stringIntegerMap.put(appId, version);
                return true;
            }

            @Override
            public int version(String appId) {
                return stringIntegerMap.get(appId);
            }
        };

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);

        return requestHandler;
    }

    private RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        ReflectionTestUtils.setField(requestWatchExecutor, "gRpcParamsConfig", gRpcParamsConfig);
        return requestWatchExecutor;
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
    public void hearBeatTest() {
        Assert.assertTrue(null != requestWatchExecutor.watcher() && requestWatchExecutor.watcher().isOnServe());
        int i = 0;
        int max = 10000;
        while (i < max) {
            Assert.assertTrue(System.currentTimeMillis() - requestWatchExecutor.watcher().heartBeat()
                    < gRpcParamsConfig.getDefaultHeartbeatTimeout());

            logger.debug("current - heartBeat : {}", System.currentTimeMillis() - requestWatchExecutor.watcher().heartBeat());
            i ++;
            TimeWaitUtils.wakeupAfter(1, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void registerTest() {
        String appId = "registerTest";
        int version = 1;

        boolean ret = requestHandler.register(appId, version);
        Assert.assertTrue(ret);
        /**
         * 重复注册
         */
        ret = requestHandler.register(appId, version);
        Assert.assertTrue(ret);

        Assert.assertNotNull(requestWatchExecutor.watcher().watches());

        Assert.assertEquals(1, requestWatchExecutor.watcher().watches().size());

        WatchElement w = requestWatchExecutor.watcher().watches().get(appId);

        Assert.assertNotNull(w);

        Assert.assertEquals(appId, w.getAppId());
        Assert.assertEquals(version, w.getVersion());

        w = requestWatchExecutor.watcher().watches().get(appId);

        Assert.assertEquals(WatchElement.AppStatus.Confirmed, w.getStatus());
    }

    @Test
    public void registerTimeoutTest() {
        String appId = "registerTimeoutTest";
        int version = 1;
        MockServer.isTestOk = false;
        /**
         * 重复注册
         */
        boolean ret = requestHandler.register(appId, version);
        Assert.assertTrue(ret);

        Assert.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                !requestWatchExecutor.watcher().watches().isEmpty());
        requestWatchExecutor.watcher().watches().entrySet().forEach(
                w -> {
                    Assert.assertNotEquals(WatchElement.AppStatus.Confirmed, w.getValue().getStatus());
                }
        );

        new Thread(() -> {
            String uid = requestWatchExecutor.watcher().uid();
            StreamObserver observer = requestWatchExecutor.watcher().observer();
            while (true) {
                if (null == requestWatchExecutor.watcher().uid()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            MockServer.isTestOk = true;
            boolean result = requestHandler.register(appId, version);
            Assert.assertTrue(result);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Assert.assertNotEquals(observer.toString(), requestWatchExecutor.watcher().observer().toString());
            Assert.assertNotEquals(uid, requestWatchExecutor.watcher().uid());

            Assert.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                    !requestWatchExecutor.watcher().watches().isEmpty());

            requestWatchExecutor.watcher().watches().entrySet().forEach(
                    w -> {
                        Assert.assertEquals(WatchElement.AppStatus.Confirmed, w.getValue().getStatus());
                    }
            );
        }).start();
    }
}
