package com.xforceplus.ulraman.oqsengine.metadata.recover.server;


import static com.xforceplus.ulraman.oqsengine.metadata.recover.Constant.*;

/**
 * desc :
 * name : CacheRecoverMockServer
 *
 * @author : xujia
 * date : 2021/4/7
 * @since : 1.8
 */
public class CacheRecoverMockServer extends BaseResponse {

    public void waitForClientClose() throws InterruptedException {
        initServer(PORT);
        Thread.sleep(3_000);

        isServerOk = true;

        mockEntityClassGenerator.reset(TEST_START_VERSION, TEST_ENTITY_CLASS_ID);
        Thread thread = new Thread(() -> {
            while (!isClientClosed) {
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
