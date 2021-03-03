package com.xforceplus.ultraman.oqsengine.meta.server;

import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * desc :
 * name : TestServerStart
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestServerStart {

    @Autowired
    private EntityClassGeneratorTestImpl entityClassGeneratorTest;

    @Resource
    private GRpcServer gRpcServer;


    @Test
    public void testStart() throws InterruptedException {
        Assert.assertTrue(gRpcServer.isStart);

        Thread.sleep(3_000);
    }
}
