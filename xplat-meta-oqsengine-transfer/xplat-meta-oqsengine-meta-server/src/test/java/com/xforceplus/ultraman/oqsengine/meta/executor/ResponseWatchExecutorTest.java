package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;

import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.*;

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

    private static final String clientId = "oqs-test";

    @BeforeEach
    public void before() {
        responseWatchExecutor = new ResponseWatchExecutor();
    }

    @AfterEach
    public void after() {
        responseWatchExecutor.stop();
    }

    @Test
    public void heartBeatTest() {

        String appId = "heartBeatTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();
        responseWatchExecutor.add(clientId, uid, streamObserver(), new WatchElement(appId, env, version, Init), false);
        responseWatchExecutor.watcher(uid).resetHeartBeat();

        Assertions.assertTrue(System.currentTimeMillis() - responseWatchExecutor.watcher(uid).heartBeat() <= 1);
    }

    @Test
    public void updateTest() {
        String appId = "updateTest";
        String env = "test";
        int version = 10;
        String uid = UUID.randomUUID().toString();

        boolean result = responseWatchExecutor.update(uid, new WatchElement(appId, env, version, Init));
        Assertions.assertFalse(result);

        responseWatchExecutor.add(clientId, uid, streamObserver(), new WatchElement(appId, env, version, Init), false);
        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
        Assertions.assertNotNull(watcher);

        List<Cases<Boolean>> cases = new ArrayList<>();
        /**
         * 修改为一个低版本
         */
        cases.add(new Cases<Boolean>(new WatchElement(appId, env, version - 1, Init), false));
        /**
         * 修改为一个相同的版本、状态
         */
        cases.add(new Cases<Boolean>(new WatchElement(appId, env, version, Init), false));
        /**
         * 修改为一个高状态
         */
        cases.add(new Cases<Boolean>(new WatchElement(appId, env, version, Confirmed), true));
        /**
         * 修改为一个高版本
         */
        cases.add(new Cases<Boolean>(new WatchElement(appId, env, version + 1, Init), true));

        cases.forEach(
                cas -> {
                    Assertions.assertEquals(cas.getExpected(), responseWatchExecutor.update(uid, cas.getWatchElement()));
                }
        );

        responseWatchExecutor.release(uid);

        watcher = responseWatchExecutor.watcher(uid);
        Assertions.assertNull(watcher);
    }

    @Test
    public void releaseTest() {
        String appId = "addTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();
        String clientId1 = "oqs-test1";

        String appId2 = "addTest";
        int version2 = 1;
        String uid2 = UUID.randomUUID().toString();
        String clientId2 = "oqs-test2";

        responseWatchExecutor.add(clientId1, uid, streamObserver(), new WatchElement(appId, env, version, Init), false);

        responseWatchExecutor.add(clientId2, uid2, streamObserver(), new WatchElement(appId2, env, version2, Init), false);

        responseWatchExecutor.release(uid);

        ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
        Assertions.assertNull(watcher);

        watcher = responseWatchExecutor.watcher(uid2);
        Assertions.assertNotNull(watcher);

        Set<String> ret = responseWatchExecutor.appWatchers(appId, env);
        Assertions.assertNotNull(ret);
        Assertions.assertEquals(1, ret.size());
        for(String s : ret) {
            Assertions.assertEquals(uid2, s);
        }
        responseWatchExecutor.release(uid2);

        watcher = responseWatchExecutor.watcher(uid2);
        Assertions.assertNull(watcher);
    }

    @Test
    public void addTest() {
        String appId = "addTest";
        String env = "test";
        int version = 1;
        String uid = UUID.randomUUID().toString();

        for (int i = 0; i < 3; i++) {
            responseWatchExecutor.add(clientId, uid, streamObserver(), new WatchElement(appId, env, version, Init), false);
            ResponseWatcher watcher = responseWatchExecutor.watcher(uid);
            Assertions.assertNotNull(watcher);

            WatchElement other = new WatchElement(appId, env, version + 1, Confirmed);
            Assertions.assertTrue(watcher.onWatch(other));

            /**
             * 重复写一条, 将失败
             */
            responseWatchExecutor.add(clientId, uid, streamObserver(), other, false);
            watcher = responseWatchExecutor.watcher(uid);
            Assertions.assertNotNull(watcher);
            /**
             * 通过onWatch判断版本是否重复添加成功
             */
            Assertions.assertTrue(watcher.onWatch(other));


            responseWatchExecutor.update(uid, other);
            watcher = responseWatchExecutor.watcher(uid);
            Assertions.assertNotNull(watcher);

            /**
             * 通过onWatch判断版本是否重复添加成功
             */
            Assertions.assertFalse(watcher.onWatch(other));

            responseWatchExecutor.release(uid);

            watcher = responseWatchExecutor.watcher(uid);
            Assertions.assertNull(watcher);
        }
    }

    @Test
    public void needTest() {
        List<AbstractMap.SimpleEntry<String, Cases<Integer>>> cases = new ArrayList<>();

        String expectedUid1 = UUID.randomUUID().toString();
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid1, new Cases<Integer>(new WatchElement("test1", "test", 1, Register), 0)));
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid1, new Cases<Integer>(new WatchElement("test2", "prod", 1, Register), 1)));

        String expectedUid2 = UUID.randomUUID().toString();
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid2, new Cases<Integer>(new WatchElement("test2", "prod", 2, Register), 2)));
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid2, new Cases<Integer>(new WatchElement("test3", "test", 5, Register), 3)));

        String expectedUid3 = UUID.randomUUID().toString();
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid3, new Cases<Integer>(new WatchElement("test2", "prod", 4, Register), 4)));
        cases.add(new AbstractMap.SimpleEntry<String, Cases<Integer>>(expectedUid3, new Cases<Integer>(new WatchElement("test3", "prod", 5, Register), 5)));

        for (AbstractMap.SimpleEntry<String, Cases<Integer>> cas : cases) {
            responseWatchExecutor.add(clientId, cas.getKey(), streamObserver(), cas.getValue().getWatchElement(), false);
        }

        int actualCount = responseWatchExecutor.watcher(expectedUid1).watches().size() +
                            responseWatchExecutor.watcher(expectedUid2).watches().size() +
                                responseWatchExecutor.watcher(expectedUid3).watches().size();

        Assertions.assertEquals(cases.size(), actualCount);

        /**
         * pos 0
         */
        WatchElement w = new WatchElement("test1", "test", 2, null);
        check(w, Collections.singletonList(cases.get(0)));

        /**
         * pos 1
         */
        w = new WatchElement("test1", "prod", 2, null);
        check(w, new ArrayList<>());

        /**
         * pos 1, 2
         */
        w = new WatchElement("test2", "prod", 3, null);
        check(w, Arrays.asList(cases.get(1), cases.get(2)));

        /**
         * pos 1, 2
         */
        w = new WatchElement("test2", "prod", 4, null);
        check(w, Arrays.asList(cases.get(1), cases.get(2)));

        /**
         * pos 1, 2, 4
         */
        w = new WatchElement("test2", "prod", 5, null);
        check(w, Arrays.asList(cases.get(1), cases.get(2), cases.get(4)));

        /**
         * pos 5
         */
        w = new WatchElement("test3", "prod", 6, null);
        check(w, Collections.singletonList(cases.get(5)));
    }

    private void check(WatchElement w, List<AbstractMap.SimpleEntry<String, Cases<Integer>>> expectedList) {
        List<ResponseWatcher> needs = responseWatchExecutor.need(w);
        Assertions.assertEquals(expectedList.size(), needs.size());

        for(ResponseWatcher r : needs) {
            /**
             * 验证
             */
            Assertions.assertTrue(r.onWatch(w));
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

    private static class Cases<T> {
        private String appId;
        private int version;
        private String env;
        private WatchElement watchElement;
        private T expected;

        public Cases(WatchElement watchElement, T expected) {
            this.watchElement = watchElement;
            this.expected = expected;
            this.appId = watchElement.getAppId();
            this.version = watchElement.getVersion();
            this.env = watchElement.getEnv();
        }

        public Cases(String appId, int version, String env, WatchElement watchElement, T expected) {
            this.appId = appId;
            this.version = version;
            this.env = env;
            this.watchElement = watchElement;
            this.expected = expected;
        }

        public String getAppId() {
            return appId;
        }

        public int getVersion() {
            return version;
        }

        public String getEnv() {
            return env;
        }

        public T getExpected() {
            return expected;
        }

        public WatchElement getWatchElement() {
            return watchElement;
        }

    }
}

