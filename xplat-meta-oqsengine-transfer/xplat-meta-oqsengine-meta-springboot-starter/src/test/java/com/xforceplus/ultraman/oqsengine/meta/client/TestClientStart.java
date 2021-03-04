package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

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

    Thread serverThread;

    boolean isLocalTest = true;

    @Before
    public void before() throws InterruptedException {
        if (isLocalTest) {
            buildServer();
        }
        Thread.sleep(1_000);
    }
    @After
    public void after() throws InterruptedException {
        if (isLocalTest) {
            ThreadUtils.shutdown(serverThread, 1);

            Thread.sleep(5_000);
        }
    }

    @Test
    public void test() throws InterruptedException {
        if (!isLocalTest) {
            boolean ret =
                    requestHandler.register(new WatchElement("7", "0", -1, WatchElement.AppStatus.Register));

            Assert.assertTrue(ret);
        }

        Thread.sleep(5_000);
    }

    private void buildServer() {
        MockServer mockServer = new MockServer();
        try {
            serverThread = ThreadUtils.create(() -> {
                try {
                    NettyServerBuilder
                            .forPort(8082).directExecutor().addService(mockServer).build().start();
                } catch (IOException e) {
                    System.exit(-1);
                }
                return true;
            });
            serverThread.start();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
