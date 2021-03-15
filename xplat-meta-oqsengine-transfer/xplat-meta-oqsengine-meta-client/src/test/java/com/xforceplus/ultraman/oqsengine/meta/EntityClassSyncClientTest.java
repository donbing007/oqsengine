package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.MockGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockServer;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : EntityClassSyncClientTest
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
public class EntityClassSyncClientTest extends BaseTest {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncClientTest.class);

    private EntityClassSyncClient entityClassSyncClient;

    private MockGRpcClient mockGRpcClient;

    @Before
    public void before() {
        mockGRpcClient = new MockGRpcClient();

        baseInit();

        entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", mockGRpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "gRpcParamsConfig", gRpcParamsConfig);
    }

    @After
    public void after() throws InterruptedException {
        entityClassSyncClient.stop();

        ExecutorHelper.shutdownAndAwaitTermination(executorService, 3600);
    }

    public void start() throws InterruptedException {
        entityClassSyncClient.start();

        Thread.sleep(5_000);
    }

    @Test
    public void hearBeatTest() throws InterruptedException {
        start();
        Assert.assertTrue(null != requestWatchExecutor.watcher() && requestWatchExecutor.watcher().isActive());
        int i = 0;
        int max = 40;
        while (i < max) {
            Assert.assertTrue(System.currentTimeMillis() - requestWatchExecutor.watcher().heartBeat()
                    < gRpcParamsConfig.getDefaultHeartbeatTimeout());

            logger.debug("current - heartBeat : {}", System.currentTimeMillis() - requestWatchExecutor.watcher().heartBeat());
            i++;
            TimeWaitUtils.wakeupAfter(1, TimeUnit.SECONDS);
        }
    }

    @Test
    public void registerTest() throws InterruptedException {
        start();
        String appId = "registerTest";
        String env = "test";
        int version = 1;

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, WatchElement.AppStatus.Register));
        Assert.assertTrue(ret);
        /**
         * 重复注册
         */
        ret = requestHandler.register(new WatchElement(appId, env, version, WatchElement.AppStatus.Register));
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
    public void forgotToRegisterTest() throws InterruptedException {
        String appId = "forgotToRegisterTest";
        String env = "test";
        int version = 1;

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
        Assert.assertFalse(ret);
        Assert.assertEquals(1, ((SyncRequestHandler) requestHandler).getForgotQueue().size());
        WatchElement element = ((SyncRequestHandler) requestHandler).getForgotQueue().peek();
        Assert.assertNotNull(element);
        Assert.assertEquals(appId, element.getAppId());
        Assert.assertEquals(version, element.getVersion());
        Assert.assertEquals(WatchElement.AppStatus.Init, element.getStatus());

        start();

        /**
         * 设置服务端onNext不可用
         */
        MockServer.isTestOk = false;

        /**
         * 模拟超时重连
         */
        int loops = 0;
        String uid = requestWatchExecutor.watcher().uid();
        while (loops < 60) {
            if (null == requestWatchExecutor.watcher().uid() ||
                    !uid.equals(requestWatchExecutor.watcher().uid())) {
                break;
            }
            try {
                loops++;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 设置服务端onNext可用
         */
        MockServer.isTestOk = true;

        loops = 0;
        while (loops < 10) {
            if (((SyncRequestHandler) requestHandler).getForgotQueue().size() == 0) {
                break;
            }

            try {
                loops++;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Assert.assertEquals(1, requestWatchExecutor.watcher().watches().size());
        element = requestWatchExecutor.watcher().watches().get(appId);
        loops = 0;
        while (loops < 10) {
            if (element.getStatus().equals(WatchElement.AppStatus.Confirmed)) {
                break;
            }
            try {
                loops++;
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void registerTimeoutTest() throws InterruptedException {
        start();
        String appId = "registerTimeoutTest";
        String env = "test";
        int version = 1;
        /**
         * 设置服务端onNext不可用
         */
        MockServer.isTestOk = false;

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, WatchElement.AppStatus.Register));
        Assert.assertTrue(ret);

        Assert.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                !requestWatchExecutor.watcher().watches().isEmpty());
        requestWatchExecutor.watcher().watches().forEach(
                (key, value) -> Assert.assertNotEquals(WatchElement.AppStatus.Confirmed, value.getStatus())
        );

        String uid = requestWatchExecutor.watcher().uid();
        StreamObserver observer = requestWatchExecutor.watcher().observer();
        /**
         * 模拟超时重连
         */
        int count = 0;
        while (count < 60) {
            if (null == requestWatchExecutor.watcher().uid() ||
                    !uid.equals(requestWatchExecutor.watcher().uid())) {
                break;
            }
            try {
                count++;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 设置服务端onNext可用
         */
        MockServer.isTestOk = true;

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(requestWatchExecutor.watcher().observer());
        Assert.assertNotEquals(observer.toString(), requestWatchExecutor.watcher().observer().toString());
        Assert.assertNotNull(requestWatchExecutor.watcher().uid());
        Assert.assertNotEquals(uid, requestWatchExecutor.watcher().uid());

        Assert.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                !requestWatchExecutor.watcher().watches().isEmpty());

        WatchElement element = requestWatchExecutor.watcher().watches().get(appId);
        count = 0;
        while (count < 10) {
            if (element.getStatus().equals(WatchElement.AppStatus.Confirmed)) {
                break;
            }
            try {
                count++;
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
