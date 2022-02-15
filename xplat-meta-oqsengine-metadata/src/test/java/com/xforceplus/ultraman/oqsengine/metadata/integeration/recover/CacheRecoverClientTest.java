package com.xforceplus.ultraman.oqsengine.metadata.integeration.recover;

import static com.xforceplus.ultraman.oqsengine.metadata.Constant.IS_CLIENT_CLOSED;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.IS_SERVER_OK;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_APP_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_ENTITY_CLASS_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_ENV;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_START_VERSION;

import com.xforceplus.ultraman.oqsengine.metadata.MockerRequestClientHelper;
import com.xforceplus.ultraman.oqsengine.metadata.integeration.recover.server.CacheRecoverMockServer;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * desc :.
 * name : CacheRecoverClientTest
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
@Disabled
public class CacheRecoverClientTest extends MockerRequestClientHelper {

    private CacheRecoverMockServer cacheRecoverMockServer = new CacheRecoverMockServer();

    /**
     * 准备.
     */
    @BeforeEach
    public void before() throws Exception {
        new Thread(() -> {
            try {
                cacheRecoverMockServer.waitForClientClose();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("waitForClientClose failed.");
            }
        }).start();

        init(true);
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

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        super.destroy();
        IS_CLIENT_CLOSED = true;
    }

    //  测试connect sdk中持有版本信息，cache中被清理，是否会重新拉取
    @Test
    public void testClientHasCacheLost() throws InterruptedException, IllegalAccessException {
        clientHasCacheLost();
    }

    private void clientHasCacheLost() throws InterruptedException, IllegalAccessException {
        //  第一次获取need后
        needAndLoad();

        //  删除缓存中当前版本信息
        MetaInitialization.getInstance().getCacheExecutor().clean(TEST_APP_ID, TEST_START_VERSION, true);

        Thread.sleep(1_000);

        //  再次获取则为空
        Optional<IEntityClass> entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(TEST_ENTITY_CLASS_ID, "");

        Assertions.assertFalse(entityClassOp.isPresent());

        //  此时再次执行need后将获取到当前版本
        needAndLoad();
    }

    private void needAndLoad() throws IllegalAccessException {
        int version = MetaInitialization.getInstance().getMetaManager().need(TEST_APP_ID, TEST_ENV);

        Assertions.assertEquals(TEST_START_VERSION, version);

        Optional<IEntityClass> entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(TEST_ENTITY_CLASS_ID, "");

        Assertions.assertTrue(entityClassOp.isPresent());
    }
}
