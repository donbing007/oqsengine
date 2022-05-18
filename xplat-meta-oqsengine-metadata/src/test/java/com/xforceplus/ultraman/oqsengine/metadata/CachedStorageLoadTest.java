package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CachedStorageLoadTest extends AbstractMetaTestHelper {

    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
    }

    @Test
    @Disabled("耗时65s+的测试，平时关闭")
    public void test() throws IllegalAccessException, InterruptedException {

        String expectedAppId = "testLoad";
        String expectedAppCode = "loadByEntityRefTest";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;

        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedAppCode, expectedVersion, expectedEntityStorageList);
        mockRequestHandler.invoke(entityClassSyncResponse, null);

        int currentVersion = expectedVersion + 1;

        Optional<IEntityClass> entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, currentVersion, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(expectedAppId, expectedAppCode, currentVersion, expectedEntityStorageList);

        mockRequestHandler.invoke(entityClassSyncResponse, null);

        //  睡眠45S，可以获取到当前版本，因为旧版本可以持有1分钟
        Thread.sleep(45_000);
        //  一分钟以内可以获取
        entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, expectedVersion + 1, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());

        Thread.sleep(20_000);
        //  一分钟以外无法获取
        entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, expectedVersion + 1, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertFalse(entityClassOp.isPresent());

        int nextVersion = currentVersion + 1;
        entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(expectedId, nextVersion, GeneralConstant.PROFILE_CODE_1.getKey());
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());
    }
}
