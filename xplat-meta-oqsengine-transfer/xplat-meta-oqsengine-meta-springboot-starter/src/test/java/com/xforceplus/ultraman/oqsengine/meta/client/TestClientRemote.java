package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * desc :
 * name : TestClientRemote
 *
 * @author : xujia
 * date : 2021/3/5
 * @since : 1.8
 */
@ActiveProfiles("clientRemote")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestClientRemote {

    @Autowired
    private IRequestHandler requestHandler;


    boolean ifTest = false;

    @Before
    public void before() throws InterruptedException {
        Thread.sleep(1_000);
    }

    @After
    public void after() throws InterruptedException {
        Thread.sleep(3_000);
    }

    @Test
    public void test() throws InterruptedException {
        if (ifTest) {
            boolean ret =
                    requestHandler.register(new WatchElement("7", "0", -1, WatchElement.AppStatus.Register));

            Assert.assertTrue(ret);

            Thread.sleep(10000_000);
        }
    }
}
