package com.xforceplus.ultraman.oqsengine.meta;


import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerMetricsInfo;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class MetricsTest extends BaseInit {

    private static final String[] baseClients = {"MetricsTest0", "MetricsTest1", "MetricsTest2"};

    private int testClientSize = 3;

    private MultiClientSyncTest.StreamEvent[] streamEvents = new MultiClientSyncTest.StreamEvent[testClientSize];

    List<List<WatchElement>> expectedWatchElement = Arrays.asList(
        Arrays.asList(
            new WatchElement("testApp1-" + 0, "0", 1, Confirmed),
            new WatchElement("testApp1-" + 1, "1", 3, Confirmed),
            new WatchElement("testApp1-" + 2, "2", 2, Confirmed)
        ),
        Arrays.asList(
            new WatchElement("testApp1-" + 2, "1", 1, Confirmed),
            new WatchElement("testApp1-" + 1, "1", 3, Confirmed),
            new WatchElement("testApp1-" + 3, "0", 2, Confirmed)
        ),
        Arrays.asList(
            new WatchElement("testApp1-" + 4, "1", 1, Confirmed),
            new WatchElement("testApp1-" + 2, "1", 3, Confirmed),
            new WatchElement("testApp1-" + 1, "0", 2, Confirmed)
        )
    );


    @BeforeEach
    public void before() throws InterruptedException {

        String host = "localhost";
        int port = 8899;

        initServer(port);
        Thread.sleep(1_000);

        for (int i = 0; i < testClientSize; i++) {
            MockerSyncClient mockerSyncClient = initClient(host, port);
            streamEvents[i] = new MultiClientSyncTest.StreamEvent(mockerSyncClient, mockerSyncClient.responseEvent(), UUID
                .randomUUID().toString());
        }

        init();
    }

    @AfterEach
    public void after() {
        for (int i = 0; i < testClientSize; i++) {
            streamEvents[i].getMockerSyncClient().stop();
        }
        stopServer();
    }

    @Test
    public void test() throws InterruptedException {
        /**
         * 注册
         */
        for (int i = 0; i < testClientSize; i++) {
            StreamEvent streamEvent = streamEvents[i];
            for (Map.Entry<String, WatchElement> entry : streamEvent.getWatchElements().entrySet()) {
                streamEvent.getStreamObserver().onNext(buildRequest(entry.getValue(), baseClients[i], streamEvent.getUid(), RequestStatus.REGISTER));
                Thread.sleep(3000);

                /**
                 * 判断是否注册成功
                 */
                assertEquals(entry.getValue(), streamEvent.getMockerSyncClient().getWatchElementMap().get(entry.getValue().getAppId()));
            }
        }

        Optional<ServerMetricsInfo> metricsInfoOptional = serverMetrics.showMetrics();
        Assertions.assertTrue(metricsInfoOptional.isPresent());

        ServerMetricsInfo smi = metricsInfoOptional.get();

        Assertions.assertEquals(testClientSize, smi.getClientWatches().size());

        smi.getClientWatches().forEach(
            cw -> {
                int j = -1;
                for (int i = 0; i < testClientSize; i ++) {
                    if (baseClients[i].equals(cw.getClientId())) {
                        j = i;
                        break;
                    }
                }
                Assertions.assertNotEquals(-1, j);
                List<WatchElement> expectedList = expectedWatchElement.get(j);
                for (WatchElement watchElement : expectedList) {
                    Optional<WatchElement> result =
                        cw.getWatches().stream().filter( w -> w.getAppId().equals(watchElement.getAppId())
                                                                && w.getVersion() == watchElement.getVersion()
                                                                && w.getEnv().equals(watchElement.getEnv()))
                                                .findFirst();
                    Assertions.assertTrue(result.isPresent());
                }

            }
        );


    }

    private void assertEquals(WatchElement expected, WatchElement actual) {
        Assertions.assertEquals(expected.getAppId(), actual.getAppId());
        Assertions.assertEquals(expected.getEnv(), actual.getEnv());
        Assertions.assertEquals(expected.getVersion(), actual.getVersion());
        Assertions.assertEquals(Confirmed, actual.getStatus());
    }


    private void init() {
        for (int i = 0; i < testClientSize; i++) {

            List<WatchElement> elements = expectedWatchElement.get(i);
            for (WatchElement watchElement : elements) {
                streamEvents[i].getWatchElements().put(watchElement.getAppId(), watchElement);
            }
        }
    }
}
