package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaTestHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * desc :.
 * name : EntityClassSyncExecutorTest
 *
 * @author : xujia 2021/2/22
 * @since : 1.8
 */

public class EntityClassSyncExecutorTest extends MetaTestHelper {


    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
    }

    @Test
    public void syncTest()
        throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException,
        JsonProcessingException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;

        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncRspProto entityClassSyncRspProto =
            EntityClassSyncProtoBufMocker.Response.entityClassSyncRspProtoGenerator(expectedEntityStorageList);

        boolean ret =
            MetaInitialization.getInstance().getEntityClassSyncExecutor().sync(expectedAppId, expectedVersion, entityClassSyncRspProto);

        Assertions.assertTrue(ret);

        int newVersion = expectedVersion + 1;
        ret =
            MetaInitialization.getInstance().getEntityClassSyncExecutor().sync(expectedAppId, newVersion, entityClassSyncRspProto);

        Assertions.assertTrue(ret);

        Thread.sleep(70_000);

        Method m0 = MetaInitialization.getInstance().getCacheExecutor().getClass()
            .getDeclaredMethod("getFromLocal", long.class, int.class);
        m0.setAccessible(true);

        Method m1 = MetaInitialization.getInstance().getCacheExecutor().getClass()
            .getDeclaredMethod("getOneFromRemote", long.class, int.class);
        m1.setAccessible(true);


        Assertions.assertEquals(newVersion, MetaInitialization.getInstance().getCacheExecutor().version(expectedAppId));

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            /*
             * 本地缓存中已不存在
             */
            EntityClassStorage notExists =
                (EntityClassStorage) m0.invoke(MetaInitialization.getInstance().getCacheExecutor(), new Object[] {e.getSelf(), expectedVersion});
            Assertions.assertNull(notExists);

            /*
             * Remote缓存中也不存在
             */
            try {
                m1.invoke(MetaInitialization.getInstance().getCacheExecutor(), e.getSelf(), expectedVersion);
            } catch (Exception ex) {
                //  ignore
            }

            /*
             * 当前活动的
             */
            EntityClassStorage exists =
                (EntityClassStorage) m1.invoke(MetaInitialization.getInstance().getCacheExecutor(), new Object[] {e.getSelf(), newVersion});
            Assertions.assertNotNull(exists);

            MetaInitialization.getInstance().getCacheExecutor().read(e.getSelf());

            exists =
                (EntityClassStorage) m0.invoke(MetaInitialization.getInstance().getCacheExecutor(), new Object[] {e.getSelf(), newVersion});
            Assertions.assertNotNull(exists);
        }
    }
}
