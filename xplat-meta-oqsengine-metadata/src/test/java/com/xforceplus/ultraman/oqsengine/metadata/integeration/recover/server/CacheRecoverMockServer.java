package com.xforceplus.ultraman.oqsengine.metadata.integeration.recover.server;


import static com.xforceplus.ultraman.oqsengine.metadata.Constant.IS_CLIENT_CLOSED;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.IS_SERVER_OK;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.LOCAL_PORT;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_ENTITY_CLASS_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_START_VERSION;

/**
 * desc :.
 * name : CacheRecoverMockServer
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class CacheRecoverMockServer extends BaseResponse {

    /**
     * 等等结束.
     */
    public void waitForClientClose() throws InterruptedException {
        initServer(LOCAL_PORT);
        Thread.sleep(3_000);

        IS_SERVER_OK = true;

        mockEntityClassGenerator.reset(TEST_START_VERSION, TEST_ENTITY_CLASS_ID);
        Thread thread = new Thread(() -> {
            while (!IS_CLIENT_CLOSED) {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        thread.join();


        stopServer();
    }
}
