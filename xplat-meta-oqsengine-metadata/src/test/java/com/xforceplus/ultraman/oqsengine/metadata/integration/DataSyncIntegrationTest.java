package com.xforceplus.ultraman.oqsengine.metadata.integration;


import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.metadata.integration.integration.AbstractIntegrationConfig;
import com.xforceplus.ultraman.oqsengine.metadata.integration.integration.ConstantConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.test.tools.annotation.BocpExtension;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@BocpExtension
public class DataSyncIntegrationTest extends AbstractIntegrationConfig {

    private static final int MAX_LOOPS = 60;
    private static final boolean TEST_OPEN = true;
    private static int testVersion = ConstantConfig.DEFAULT_TEST_START_VERSION;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        initAll(TEST_OPEN);
    }

    @AfterEach
    public void afterEach() {
        destroyAll(TEST_OPEN);
    }

    @Test
    public void test() throws InterruptedException {
        if (TEST_OPEN) {
            List<EntityClassInfo> entityClassInfoList = init();

            Optional<IEntityClass> entityClassOptional = storageMetaManager.load(ConstantConfig.TEST_ENTITY_CLASS_ID);
            Assertions.assertTrue(entityClassOptional.isPresent());
            IEntityClassChecker.check(entityClassOptional.get(), entityClassInfoList);
        }
    }

    private List<EntityClassInfo> init() throws InterruptedException {
        List<EntityClassInfo> entityClassInfoList = null;
        new Thread(() -> {
            testVersion = storageMetaManager.need(ConstantConfig.TEST_APP_ID, ConstantConfig.TEST_ENV);
        }).start();

        int count = 0;
        while (count < MAX_LOOPS) {
            entityClassInfoList = enhancedSyncExecutor.getEntityClasses(ConstantConfig.TEST_APP_ID, testVersion);
            if (null != entityClassInfoList && !entityClassInfoList.isEmpty()) {
                //  获取到了当前需要比较的entityClassList
                break;
            }
            Thread.sleep(5_000);
            count++;
        }
        Assertions.assertNotEquals(MAX_LOOPS, count, "loops too much, data synced failed.");

        return entityClassInfoList;
    }
}
