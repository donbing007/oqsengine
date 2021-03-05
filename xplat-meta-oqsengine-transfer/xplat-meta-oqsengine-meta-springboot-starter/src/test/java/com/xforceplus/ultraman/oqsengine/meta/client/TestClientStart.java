package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.Commons;
import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.Commons.caseHeartBeat;

/**
 * desc :
 * name : TestClientStart
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@ActiveProfiles("client")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestClientStart {

    @Autowired
    private IRequestHandler requestHandler;

    private static final Map<String, BiFunction<String, WatchElement, Boolean>> functions = new HashMap<>();

    @Before
    public void before() throws InterruptedException {

        Thread.sleep(1_000);

        functions.put(caseHeartBeat, this::heartBeatTest);
    }

    @After
    public void after() throws InterruptedException {
        Thread.sleep(1_000);
    }


    @Test
    public void test() {
        for (Map.Entry<String, WatchElement> e : Commons.cases.entrySet()) {
            BiFunction<String, WatchElement, Boolean> f = functions.get(e.getKey());
            if (null != f) {
                System.out.println(String.format("start test [%s]...", e.getKey()));
                Assert.assertTrue(f.apply(e.getKey(), e.getValue()));
                System.out.println(String.format("successful test [%s]...", e.getKey()));
            }
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    public boolean heartBeatTest(String caseName, WatchElement w) {
        boolean ret = requestHandler.register(w);

        Assert.assertTrue(ret);

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /**
         * 测试
         */
        return assertElement(caseHeartBeat, WatchElement.AppStatus.Confirmed,
                                requestHandler.watchExecutor().watcher().watches().get(caseName));
    }

    public boolean assertElement(String caseName, WatchElement.AppStatus appStatus, WatchElement w) {
        Assert.assertEquals(w.getAppId(), Commons.cases.get(caseName).getAppId());
        Assert.assertEquals(w.getStatus(), Commons.cases.get(caseName).getStatus());
        Assert.assertEquals(w.getVersion(), Commons.cases.get(caseName).getVersion());
        Assert.assertEquals(w.getStatus(), appStatus);
        return true;
    }


}
