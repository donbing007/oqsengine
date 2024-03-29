package com.xforceplus.ultraman.oqsengine.meta;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;
import static com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassSyncRspProtoBuilder.entityClassSyncRspProtoGenerator;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockSyncEvent;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :
 * name : MultiClientSyncTest.
 *
 * @author : xujia 2021/3/2
 * @since : 1.8
 */
public class MultiClientSyncTest extends BaseInit {

    private Logger logger = LoggerFactory.getLogger(MultiClientSyncTest.class);

    private int testClientSize = 2;
    private StreamEvent[] streamEvents = new StreamEvent[testClientSize];

    @BeforeEach
    public void before() throws InterruptedException {

        String host = "localhost";
        int port = 8899;

        initServer(port);
        Thread.sleep(1_000);

        for (int i = 0; i < testClientSize; i++) {
            MockerSyncClient mockerSyncClient = initClient(host, port);
            streamEvents[i] =
                new StreamEvent(mockerSyncClient, mockerSyncClient.responseEvent(), UUID.randomUUID().toString());
        }

        init();
    }

    @AfterEach
    public void after() throws InterruptedException {
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


    private static final String baseClient = "MultiClientSyncTest";

    private void init() {

        for (int i = 0; i < testClientSize; i++) {
            /**
             * 测试通用更新、多个客户端同时更新某1个AppID+ENV的相同版本
             */
            WatchElement common = new WatchElement(commonAppId, commonEnv, commonStartVersion, Register);

            streamEvents[i].getWatchElements().put(commonAppId, common);
            /**
             * 增加expected
             */
            expectedWatchers.computeIfAbsent(commonAppId, v -> new WatchElementVisitor(common)).setVisitors(i);

            /**
             * 测试多个客户端所关注同一个AppID的ENV不一致
             */
            WatchElement selfTestDiffEnv =
                new WatchElement(privateTestDiffEnv, privateEnvPrefix + i, commonStartVersion, Register);
            streamEvents[i].getWatchElements().put(privateTestDiffEnv, selfTestDiffEnv);
            /**
             * 增加expected
             */
            expectedWatchers.computeIfAbsent(privateTestDiffEnv + i, v -> new WatchElementVisitor(selfTestDiffEnv))
                .setVisitors(i);
        }

        for (int i = 0; i < testClientSize; i++) {
            /**
             * 测试多个客户端所关注同一个AppID的不同版本
             */
            WatchElement selfTestDiffVersion = new WatchElement(privateTestDiffVersion, commonEnv, i, Register);
            streamEvents[i].getWatchElements().put(privateTestDiffVersion, selfTestDiffVersion);

            expectedWatchers
                .computeIfAbsent(privateTestDiffVersion + i, v -> new WatchElementVisitor(selfTestDiffVersion))
                .setVisitors(i);
        }

        /**
         * 增加expected
         */
        for (int i = 1; i < testClientSize; i++) {
            int j = 0;
            while (j < i) {
                expectedWatchers.get(privateTestDiffVersion + i).setVisitors(j);
                j++;
            }
        }
    }

    @Test
    public void pushTest() throws InterruptedException {
        /**
         * 注册
         */
        int i = 0;
        for (StreamEvent streamEvent : streamEvents) {
            i++;
            for (Map.Entry<String, WatchElement> entry : streamEvent.getWatchElements().entrySet()) {
                streamEvent.getStreamObserver().onNext(
                    buildRequest(entry.getValue(), baseClient + i, streamEvent.getUid(), RequestStatus.REGISTER));
                Thread.sleep(3000);
                /**
                 * 判断是否注册成功
                 */
                assertEquals(entry.getValue(),
                    streamEvent.getMockerSyncClient().getWatchElementMap().get(entry.getValue().getAppId()));
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
        Assertions.assertEquals(expected.getAppId(), actual.getAppId());
        Assertions.assertEquals(expected.getEnv(), actual.getEnv());
        Assertions.assertEquals(expected.getVersion(), actual.getVersion());
        Assertions.assertEquals(Confirmed, actual.getStatus());
    }

    private void assertNotEquals(WatchElement expected, WatchElement actual) {
        Assertions.assertTrue(
            !expected.getAppId().equals(actual.getAppId()) ||
                !expected.getEnv().equals(actual.getEnv()) ||
                expected.getVersion() != actual.getVersion()
        );
    }

    private void testByCondition(WatchElementVisitor watchElementVisitor) throws InterruptedException {
        /*
         * 当前版本 + 1, 随机的
         */
        String expectedAppId = watchElementVisitor.getWatchElement().getAppId();
        String expectedEnv = watchElementVisitor.getWatchElement().getEnv();
        int expectedVersion = watchElementVisitor.getWatchElement().getVersion() + 1;
        syncResponseHandler.push(new MockSyncEvent(expectedAppId, expectedEnv, expectedVersion,
            entityClassSyncRspProtoGenerator(new Random().nextLong())));

        Thread.sleep(1000);
        for (int i = 0; i < testClientSize; i++) {
            WatchElement w = streamEvents[i].getMockerSyncClient().getSuccess(expectedAppId);
            if (watchElementVisitor.getVisitors().contains(i)) {

                assertEquals(new WatchElement(expectedAppId, expectedEnv, expectedVersion, null), w);
            } else {
                if (null != w) {
                    assertNotEquals(new WatchElement(expectedAppId, expectedEnv, expectedVersion, null), w);
                }
            }
        }
    }
}
