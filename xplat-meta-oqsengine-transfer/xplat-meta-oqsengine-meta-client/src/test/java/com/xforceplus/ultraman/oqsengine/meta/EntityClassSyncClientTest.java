package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.MockGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockServer;
import io.grpc.stub.StreamObserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.*;

/**
 * desc :
 * name : EntityClassSyncClientTest
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
public class EntityClassSyncClientTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(EntityClassSyncClientTest.class);

    private EntityClassSyncClient entityClassSyncClient;

    private MockGRpcClient mockGRpcClient;

    @BeforeEach
    public void before() {
        mockGRpcClient = new MockGRpcClient();

        baseInit();

        entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", mockGRpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "grpcParamsConfig", gRpcParams);
    }

    @AfterEach
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
        Assertions.assertTrue(null != requestWatchExecutor.watcher() && requestWatchExecutor.watcher().isActive());
        int i = 0;
        int max = 40;
        while (i < max) {
            Assertions.assertTrue(System.currentTimeMillis() - requestWatchExecutor.watcher().heartBeat()
                    < gRpcParams.getDefaultHeartbeatTimeout());

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

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, Register));
        Assertions.assertTrue(ret);
        /**
         * 重复注册
         */
        ret = requestHandler.register(new WatchElement(appId, env, version, Register));
        Assertions.assertTrue(ret);

        Assertions.assertNotNull(requestWatchExecutor.watcher().watches());

        Assertions.assertEquals(1, requestWatchExecutor.watcher().watches().size());

        WatchElement w = requestWatchExecutor.watcher().watches().get(appId);

        Assertions.assertNotNull(w);

        Assertions.assertEquals(appId, w.getAppId());
        Assertions.assertEquals(version, w.getVersion());

        w = requestWatchExecutor.watcher().watches().get(appId);

        Assertions.assertEquals(Confirmed, w.getStatus());
    }

    @Test
    public void forgotToRegisterTest() throws InterruptedException {
        String appId = "forgotToRegisterTest";
        String env = "test";
        int version = 1;

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, Init));
        Assertions.assertFalse(ret);
        Assertions.assertEquals(1, ((SyncRequestHandler) requestHandler).getForgotQueue().size());
        WatchElement element = ((SyncRequestHandler) requestHandler).getForgotQueue().peek();
        Assertions.assertNotNull(element);
        Assertions.assertEquals(appId, element.getAppId());
        Assertions.assertEquals(version, element.getVersion());
        Assertions.assertEquals(Init, element.getStatus());

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

        Assertions.assertEquals(1, requestWatchExecutor.watcher().watches().size());
        element = requestWatchExecutor.watcher().watches().get(appId);
        loops = 0;
        while (loops < 10) {
            if (element.getStatus().equals(Confirmed)) {
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

        boolean ret = requestHandler.register(new WatchElement(appId, env, version, Register));
        Assertions.assertTrue(ret);

        Assertions.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                !requestWatchExecutor.watcher().watches().isEmpty());
        requestWatchExecutor.watcher().watches().forEach(
                (key, value) -> Assertions.assertNotEquals(Confirmed, value.getStatus())
        );

        String uid = requestWatchExecutor.watcher().uid();
        StreamObserver observer = requestWatchExecutor.watcher().observer();

        MockServer.isTestOk = true;

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MockServer.isTestOk = false;

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

        Assertions.assertNotNull(requestWatchExecutor.watcher().observer());
        Assertions.assertNotEquals(observer.toString(), requestWatchExecutor.watcher().observer().toString());
        Assertions.assertNotNull(requestWatchExecutor.watcher().uid());
        Assertions.assertNotEquals(uid, requestWatchExecutor.watcher().uid());

        Assertions.assertTrue(null != requestWatchExecutor.watcher().watches() &&
                !requestWatchExecutor.watcher().watches().isEmpty());

        WatchElement element = requestWatchExecutor.watcher().watches().get(appId);
        count = 0;
        while (count < 10) {
            if (element.getStatus().equals(Confirmed)) {
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
