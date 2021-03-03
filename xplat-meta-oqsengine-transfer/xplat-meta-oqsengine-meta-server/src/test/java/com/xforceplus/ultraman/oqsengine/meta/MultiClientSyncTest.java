package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    private MockerSyncClient[] mockerSyncClient = new MockerSyncClient[testClientSize];

    @Before
    public void before() throws InterruptedException {

        String host = "localhost";
        int port = 8899;

        initServer(port);
        Thread.sleep(1_000);

        for (int i = 0; i < testClientSize; i++) {
            mockerSyncClient[i] = initClient(host, port);
        }
    }

    @After
    public void after() {
        for (int i = 0; i < testClientSize; i++) {
            mockerSyncClient[i].stop();
        }
        stopServer();
    }

    @Test
    public void pushTest() {

    }

}
