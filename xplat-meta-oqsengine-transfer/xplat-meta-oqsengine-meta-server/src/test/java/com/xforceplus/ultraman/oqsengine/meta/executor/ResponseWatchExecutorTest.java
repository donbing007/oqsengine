package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

/**
 * desc :
 * name : ResponseWatchExecutorTest
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
public class ResponseWatchExecutorTest {

    private ResponseWatchExecutor responseWatchExecutor;

    @Before
    public void before() {
        responseWatchExecutor = new ResponseWatchExecutor();
    }

    @After
    public void after() {
        responseWatchExecutor.stop();
    }

    @Test
    public void heartBeatTest() {

        String appId = "heartBeatTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();
        responseWatchExecutor.add(uid, streamObserver(), new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
        responseWatchExecutor.watcher(uid).resetHeartBeat();

        Assert.assertTrue(System.currentTimeMillis() - responseWatchExecutor.watcher(uid).heartBeat() <= 1);
    }

    @Test
    public void updateTest() {
        String appId = "updateTest";
        String env = "test";
        int version = 10;
        String uid = UUID.randomUUID().toString();

        boolean result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
        Assert.assertFalse(result);

        responseWatchExecutor.add(uid, streamObserver(), new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
        Assert.assertNotNull(watcher);

        /**
         * 修改为一个低版本
         */
        result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version - 1, WatchElement.AppStatus.Init));
        Assert.assertFalse(result);

        /**
         * 修改为一个相同的版本、状态
         */
        result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
        Assert.assertFalse(result);

        /**
         * 修改为一个高状态
         */
        result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version, WatchElement.AppStatus.Confirmed));
        Assert.assertTrue(result);

        /**
         * 修改为一个高版本
         */
        result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version + 1, WatchElement.AppStatus.Init));
        Assert.assertTrue(result);

        responseWatchExecutor.release(uid);

        watcher = responseWatchExecutor.watcher(uid);
        Assert.assertNull(watcher);
    }

    @Test
    public void releaseTest() {
        String appId = "addTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();


        String appId2 = "addTest";
        int version2 = 1;
        String uid2 = UUID.randomUUID().toString();

        responseWatchExecutor.add(uid, streamObserver(), new WatchElement(appId, env, version, WatchElement.AppStatus.Init));

        responseWatchExecutor.add(uid2, streamObserver(), new WatchElement(appId2, env, version2, WatchElement.AppStatus.Init));

        responseWatchExecutor.release(uid);

        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
        Assert.assertNull(watcher);

        watcher = responseWatchExecutor.watcher(uid2);
        Assert.assertNotNull(watcher);

        Set<String> ret = responseWatchExecutor.appWatchers(appId, env);
        Assert.assertNotNull(ret);
        Assert.assertEquals(1, ret.size());
        for(String s : ret) {
            Assert.assertEquals(uid2, s);
        }
    }

    @Test
    public void addTest() {
        String appId = "addTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();

        for (int i = 0; i < 3; i++) {
            responseWatchExecutor.add(uid, streamObserver(), new WatchElement(appId, env, version, WatchElement.AppStatus.Init));
            ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
            Assert.assertNotNull(watcher);

            WatchElement other = new WatchElement(appId, env, version + 1, WatchElement.AppStatus.Confirmed);
            Assert.assertTrue(watcher.onWatch(other));

            /**
             * 重复写一条, 将失败
             */
            responseWatchExecutor.add(uid, streamObserver(), other);
            watcher = responseWatchExecutor.watcher(uid);
            Assert.assertNotNull(watcher);
            /**
             * 通过onWatch判断版本是否重复添加成功
             */
            Assert.assertTrue(watcher.onWatch(other));


            responseWatchExecutor.update(uid, other);
            watcher = responseWatchExecutor.watcher(uid);
            Assert.assertNotNull(watcher);

            /**
             * 通过onWatch判断版本是否重复添加成功
             */
            Assert.assertFalse(watcher.onWatch(other));

            responseWatchExecutor.release(uid);

            watcher = responseWatchExecutor.watcher(uid);
            Assert.assertNull(watcher);
        }
    }

    private StreamObserver<EntityClassSyncResponse> streamObserver() {
        return new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {
                // do nothing
            }

            @Override
            public void onError(Throwable throwable) {
                // do nothing
            }

            @Override
            public void onCompleted() {
                // do nothing
            }
        };
    }
}

