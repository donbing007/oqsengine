package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassSyncRspProtoBuilder.entityClassSyncRspProtoGenerator;

/**
 * desc :
 * name : MultiClientSyncTest
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class MultiClientSyncTest extends BaseInit {

    private int testClientSize = 2;
    private StreamEvent[] streamEvents = new StreamEvent[testClientSize];

    @Before
    public void before() throws InterruptedException {

        String host = "localhost";
        int port = 8899;

        initServer(port);
        Thread.sleep(1_000);

        for (int i = 0; i < testClientSize; i++) {
            MockerSyncClient mockerSyncClient = initClient(host, port);
            streamEvents[i] = new StreamEvent(mockerSyncClient, mockerSyncClient.responseEvent(), UUID.randomUUID().toString());
        }

        init();
    }

    @After
    public void after() {
        for (int i = 0; i < testClientSize; i++) {
            streamEvents[i].getMockerSyncClient().stop();
        }
        stopServer();
    }

    private Map<String, WatchElementVisitor> expectedWatchers = new LinkedHashMap<>();
    private String commonAppId = "all";
    private String commonEnv = "test";
    private int commonStartVersion = -1;

    /**
     * appId private
     */
    private String privateTestDiffVersion = "privateTestDiffVersion";
    private String privateTestDiffEnv = "privateTestDiffEnv";
    private String privateEnvPrefix = "privateEnvPrefix";


    private void init() {

        for (int i = 0; i < testClientSize; i++) {
            /**
             * 测试通用更新、多个客户端同时更新某1个AppID+ENV的相同版本
             */
            WatchElement common = new WatchElement(commonAppId, commonEnv, commonStartVersion, WatchElement.AppStatus.Register);

            streamEvents[i].getWatchElements().put(commonAppId, common);
            /**
             * 增加expected
             */
            expectedWatchers.computeIfAbsent(commonAppId, v -> new WatchElementVisitor(common)).setVisitors(i);

            /**
             * 测试多个客户端所关注同一个AppID的ENV不一致
             */
            WatchElement selfTestDiffEnv = new WatchElement(privateTestDiffEnv, privateEnvPrefix + i, commonStartVersion, WatchElement.AppStatus.Register);
            streamEvents[i].getWatchElements().put(privateTestDiffEnv, selfTestDiffEnv);
            /**
             * 增加expected
             */
            expectedWatchers.computeIfAbsent(privateTestDiffEnv + i, v -> new WatchElementVisitor(selfTestDiffEnv)).setVisitors(i);
        }

        for (int i = 0; i < testClientSize; i++) {
            /**
             * 测试多个客户端所关注同一个AppID的不同版本
             */
            WatchElement selfTestDiffVersion = new WatchElement(privateTestDiffVersion, commonEnv, i, WatchElement.AppStatus.Register);
            streamEvents[i].getWatchElements().put(privateTestDiffVersion, selfTestDiffVersion);

            expectedWatchers.computeIfAbsent(privateTestDiffVersion + i, v -> new WatchElementVisitor(selfTestDiffVersion)).setVisitors(i);
        }

        /**
         * 增加expected
         */
        for (int i = 1; i < testClientSize; i++) {
            int j = 0;
            while (j  < i) {
                expectedWatchers.get(privateTestDiffVersion + i).setVisitors(j);
                j ++;
            }
        }
    }


    @Test
    public void pushTest() throws InterruptedException {
        /**
         * 注册
         */
        for (StreamEvent streamEvent : streamEvents) {
            for (Map.Entry<String, WatchElement> entry : streamEvent.getWatchElements().entrySet()) {
                streamEvent.getStreamObserver().onNext(buildRequest(entry.getValue(), streamEvent.getUid(), RequestStatus.REGISTER));
                Thread.sleep(1000);
                /**
                 * 判断是否注册成功
                 */
                assertEquals(entry.getValue(), streamEvent.getMockerSyncClient().getWatchElementMap().get(entry.getValue().getAppId()));
            }
        }

        /**
         * 测试
         */
        for (Map.Entry<String, WatchElementVisitor> entry : expectedWatchers.entrySet()) {
            testByCondition(entry.getValue());
        }
    }
    private void assertEquals(WatchElement expected, WatchElement actual) {
        Assert.assertEquals(expected.getAppId(), actual.getAppId());
        Assert.assertEquals(expected.getEnv(), actual.getEnv());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
        Assert.assertEquals(WatchElement.AppStatus.Confirmed, actual.getStatus());
    }

    private void assertNotEquals(WatchElement expected, WatchElement actual) {
        Assert.assertTrue(
                !expected.getAppId().equals(actual.getAppId()) ||
                         !expected.getEnv().equals(actual.getEnv()) ||
                        expected.getVersion() != actual.getVersion()
        );
    }

    private void testByCondition(WatchElementVisitor watchElementVisitor) throws InterruptedException {
        /**
         * 当前版本 + 1, 随机的
         */
        String expectedAppId = watchElementVisitor.getWatchElement().getAppId();
        String expectedEnv = watchElementVisitor.getWatchElement().getEnv();
        int expectedVersion = watchElementVisitor.getWatchElement().getVersion() + 1;
        syncResponseHandler.push(new AppUpdateEvent("mock", expectedAppId, expectedEnv, expectedVersion,
                            entityClassSyncRspProtoGenerator(new Random().nextLong())));

        Thread.sleep(1000);
        for (int i = 0; i < testClientSize; i++) {
            WatchElement w = streamEvents[i].getMockerSyncClient().getSuccess();
            if (watchElementVisitor.getVisitors().contains(i)) {
                assertEquals(new WatchElement(expectedAppId, expectedEnv, expectedVersion, null), w);
            }
            else {
                assertNotEquals(new WatchElement(expectedAppId, expectedEnv, expectedVersion, null), w);
            }
        }
    }

    public static class WatchElementVisitor {
        private WatchElement watchElement;
        private Set<Integer> visitors;

        public WatchElementVisitor(WatchElement watchElement) {
            this.watchElement = watchElement;
            visitors = new HashSet<>();
        }

        public WatchElement getWatchElement() {
            return watchElement;
        }

        public Set<Integer> getVisitors() {
            return visitors;
        }

        public void setVisitors(Integer visitors) {
            this.visitors.add(visitors);
        }
    }

    public static class StreamEvent {
        private MockerSyncClient mockerSyncClient;
        private StreamObserver<EntityClassSyncRequest> streamObserver;
        private String uid;

        public StreamEvent(MockerSyncClient mockerSyncClient, StreamObserver<EntityClassSyncRequest> streamObserver, String uid) {
            this.mockerSyncClient = mockerSyncClient;
            this.streamObserver = streamObserver;
            this.uid = uid;
        }

        public MockerSyncClient getMockerSyncClient() {
            return mockerSyncClient;
        }

        public StreamObserver<EntityClassSyncRequest> getStreamObserver() {
            return streamObserver;
        }

        public String getUid() {
            return uid;
        }

        public Map<String, WatchElement> getWatchElements() {
            return mockerSyncClient.getWatchElementMap();
        }
    }
}
