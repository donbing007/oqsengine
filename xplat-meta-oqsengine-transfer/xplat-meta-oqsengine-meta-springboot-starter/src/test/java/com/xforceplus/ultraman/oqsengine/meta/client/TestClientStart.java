package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Rule
    public GrpcCleanupRule gRpcCleanup = new GrpcCleanupRule();


    @Before
    public void before() throws InterruptedException {
        buildServer();
        Thread.sleep(1_000);
    }

    private void buildServer() {
        MockServer mockServer = new MockServer();
        try {
            gRpcCleanup.register(NettyServerBuilder
                    .forPort(8082).directExecutor().addService(mockServer).build().start());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(100_000);
    }
}
