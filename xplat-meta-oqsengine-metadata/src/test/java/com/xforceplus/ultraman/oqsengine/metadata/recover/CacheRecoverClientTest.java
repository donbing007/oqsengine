package com.xforceplus.ultraman.oqsengine.metadata.recover;

import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.TEST_APP_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.TEST_ENTITY_CLASS_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.TEST_ENV;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.TEST_START_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.IF_TEST;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.IS_CLIENT_CLOSED;
import static com.xforceplus.ultraman.oqsengine.metadata.recover.Constant.IS_SERVER_OK;

import com.xforceplus.ultraman.oqsengine.metadata.InitBase;
import com.xforceplus.ultraman.oqsengine.metadata.recover.server.CacheRecoverMockServer;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * desc :.
 * name : CacheRecoverClientTest
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class CacheRecoverClientTest extends BaseRequest {

    private InitBase initBase;

    private CacheRecoverMockServer cacheRecoverMockServer = new CacheRecoverMockServer();

    @Before
    public void before() throws Exception {
        if (IF_TEST) {
            baseInit();

            initBase = new InitBase(requestHandler);
            initBase.init();

            new Thread(() -> {
                try {
                    cacheRecoverMockServer.waitForClientClose();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException("waitForClientClose failed.");
                }
            }).start();

            int i = 0;
            while (i < 100) {
                if (IS_SERVER_OK) {
                    entityClassSyncClient.start();

                    Thread.sleep(5_000);
                    return;
                }
                //  睡眠1秒
                Thread.sleep(1_000);
                i++;
            }
            throw new RuntimeException("test has failed due to server not start.");
        }
    }

    @After
    public void after() throws Exception {
        if (IF_TEST) {
            entityClassSyncClient.stop();
            initBase.clear();
            IS_CLIENT_CLOSED = true;
        }
    }

    //  测试connect sdk中持有版本信息，cache中被清理，是否会重新拉取
    @Test
    public void testClientHasCacheLost() throws InterruptedException {
        if (IF_TEST) {
            clientHasCacheLost();
        }
    }

    private void clientHasCacheLost() throws InterruptedException {
        //  第一次获取need后
        needAndLoad();

        //  删除缓存中当前版本信息
        initBase.cacheExecutor.clean(TEST_APP_ID, TEST_START_VERSION, true);
        //  强制再清理一次redis
        initBase.clearRedis();

        Thread.sleep(1_000);

        //  再次获取则为空
        Optional<IEntityClass> entityClassOp =
            initBase.storageMetaManager.load(TEST_ENTITY_CLASS_ID);

        Assert.assertFalse(entityClassOp.isPresent());

        //  此时再次执行need后将获取到当前版本
        needAndLoad();
    }

    private void needAndLoad() {
        int version = initBase.storageMetaManager.need(TEST_APP_ID, TEST_ENV);

        Assert.assertEquals(TEST_START_VERSION, version);

        Optional<IEntityClass> entityClassOp =
            initBase.storageMetaManager.load(TEST_ENTITY_CLASS_ID);

        Assert.assertTrue(entityClassOp.isPresent());
    }
}
