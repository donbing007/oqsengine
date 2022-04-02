package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.SystemOpsService;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops.mock.MockedCache;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.AppSimpleInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.ClientModel;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */

@ExtendWith({
    RedisContainer.class,
    MysqlContainer.class,
    ManticoreContainer.class,
    CanalContainer.class,
    SpringExtension.class
})
@ActiveProfiles("discover")
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DiscoverDevOpsServiceTest {

    @Autowired
    private SystemOpsService discoverDevOpsService;

    @MockBean(name = "keyValueStorage")
    private KeyValueStorage keyValueStorage;

    private static String expectedAppId = "discover-test";
    private static int expectedVersion = Integer.MAX_VALUE;

    private static boolean isInit = false;
    /**
     * 每个测试前初始化.
     */
    @BeforeEach
    public void before() throws IllegalAccessException, JsonProcessingException {
        if (!isInit) {
            MetaManager metaManager = new StorageMetaManager(new ClientModel());

            Collection<Field> cacheFields = ReflectionUtils.printAllMembers(metaManager);
            ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", metaManager,
                MetaInitialization.getInstance().getCacheExecutor());
            ReflectionUtils.reflectionFieldValue(cacheFields, "syncExecutor", metaManager,
                MetaInitialization.getInstance().getEntityClassSyncExecutor());
            ReflectionUtils.reflectionFieldValue(cacheFields, "asyncDispatcher", metaManager,
                CommonInitialization.getInstance().getRunner());

            Collection<Field> discoverFields = ReflectionUtils.printAllMembers(discoverDevOpsService);
            ReflectionUtils.reflectionFieldValue(discoverFields, "metaManager", discoverDevOpsService, metaManager);

            MockedCache.entityClassStorageSave(expectedAppId, expectedVersion);

            isInit = true;
        }
    }

    @AfterAll
    public static void afterAll() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void showMetaTest() {
        MetaMetrics metaMetrics = discoverDevOpsService.showMeta(expectedAppId);

        Assertions.assertTrue(metaMetrics.getMetas().size() > 0);
    }

    @Test
    public void showApplicationTest() {
        Collection<AppSimpleInfo> applicationInfo = discoverDevOpsService.appInfo();
        Assertions.assertEquals(1, applicationInfo.size());
    }

    @Test
    public void systemInfoTest() {
        Assertions.assertTrue(discoverDevOpsService.systemInfo().size() > 0);
    }

    @Test
    @Disabled("不是常用的测试,平时忽略.")
    public void test() throws InterruptedException, JsonProcessingException {
        Thread.sleep(10000_000);
    }
}
