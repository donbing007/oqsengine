package com.xforceplus.ultraman.oqsengine.metadata.integration;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.test.tools.annotation.BocpExtension;
import com.xforceplus.ultraman.test.tools.annotation.BocpOqsExtension;
import com.xforceplus.ultraman.test.tools.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.container.basic.RedisContainer;
import com.xforceplus.ultraman.test.tools.container.module.BocpContainer;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@BocpExtension
@ExtendWith({RedisContainer.class})
public class DataSyncIntegrationTest extends BaseIntegration {

    public static final int MAX_LOOPS = 60;
    public static final String TEST_APP_ID = "7";
    public static final String TEST_ENV = "0";
    public static final long TEST_ENTITY_CLASS_ID = 1275678539314814978L;
    public int testVersion = -1;
    public List<EntityClassInfo> entityClassInfoList = null;

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        if (TEST_OPEN) {
            beforeClass();

            init();

            new Thread(() -> {
                testVersion = storageMetaManager.need(TEST_APP_ID, TEST_ENV);
            }).start();

            int count = 0;
            while (count < MAX_LOOPS) {
                entityClassInfoList = enhancedSyncExecutor.getEntityClasses(TEST_APP_ID, testVersion);
                if (null != entityClassInfoList && !entityClassInfoList.isEmpty()) {
                    //  获取到了当前需要比较的entityClassList
                    break;
                }
                Thread.sleep(5_000);
                count++;
            }
            Assert.assertNotEquals("loops too much, data synced failed.", MAX_LOOPS, count);
        }
    }

    @AfterEach
    public void afterEach() {
        if (TEST_OPEN) {
            destroy();
            entityClassInfoList = null;
            afterClass();
        }
    }

    @Test
    public void test() {
        if (TEST_OPEN) {
            Optional<IEntityClass> entityClassOptional = storageMetaManager.load(TEST_ENTITY_CLASS_ID);
            Assert.assertTrue(entityClassOptional.isPresent());
            IEntityClassChecker.check(entityClassOptional.get(), entityClassInfoList);
        }
    }
}
