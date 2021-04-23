package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
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

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;

/**
 * 为了检查连接正确性的手动测试,ifTest 默认为false.
 * 在正常的测试流程中不会运行.
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

    @Test
    public void test() throws InterruptedException {
        if (ifTest) {
            boolean ret =
                    requestHandler.register(new WatchElement("7", "0", -1, Register));

            Assert.assertTrue(ret);
//
//            ret = requestHandler.register(new WatchElement("1", "0", -1, Register));

            Thread.sleep(10000_000);
        }
    }
}
