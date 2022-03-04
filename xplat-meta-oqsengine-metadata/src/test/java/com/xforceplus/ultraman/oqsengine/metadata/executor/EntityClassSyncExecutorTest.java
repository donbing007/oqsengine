package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.metadata.AbstractMetaTestHelper;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.utils.storage.CacheToStorageGenerator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

public class EntityClassSyncExecutorTest extends AbstractMetaTestHelper {

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
        throws InterruptedException, IllegalAccessException, JsonProcessingException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;

        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncRspProto entityClassSyncRspProto =
            EntityClassSyncProtoBufMocker.Response.entityClassSyncRspProtoGenerator(expectedEntityStorageList);

        int newVersion = expectedVersion + 1;

        MetaInitialization.getInstance().getEntityClassSyncExecutor()
                .sync(expectedAppId, newVersion, entityClassSyncRspProto);

        Thread.sleep(70_000);

        Assertions.assertEquals(newVersion, MetaInitialization.getInstance().getCacheExecutor().version(expectedAppId));

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            Collection<EntityClassStorage> res = CacheToStorageGenerator.toEntityClassStorages(
                    DefaultCacheExecutor.OBJECT_MAPPER,
                    MetaInitialization.getInstance().getCacheExecutor().multiRemoteRead(
                        Collections.singletonList(e.getSelf()), newVersion
                    )
                ).values();

            Assertions.assertEquals(1, res.size());
        }
    }
}
