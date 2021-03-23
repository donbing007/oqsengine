package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import io.grpc.netty.NettyServerBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;

/**
 * desc :
 * name : TestClientStartLocal
 *
 * @author : xujia
 * date : 2021/3/5
 * @since : 1.8
 */
@ActiveProfiles("clientLocal")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestClientStartLocal {

    @Autowired
    private IRequestHandler requestHandler;

    Thread serverThread;

    boolean ifTest = false;

    @Before
    public void before() throws InterruptedException {
        if (ifTest) {
            buildServer();
            Thread.sleep(1_000);
        }
    }

    @After
    public void after() throws InterruptedException {
        if (ifTest) {
            ThreadUtils.shutdown(serverThread, 1);

            Thread.sleep(3_000);
        }
    }

    @Test
    public void test() throws InterruptedException {
        if (ifTest) {
            boolean ret =
                    requestHandler.register(new WatchElement("7", "0", -1, Register));

            Assert.assertTrue(ret);

            Thread.sleep(5_000);
        }
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
