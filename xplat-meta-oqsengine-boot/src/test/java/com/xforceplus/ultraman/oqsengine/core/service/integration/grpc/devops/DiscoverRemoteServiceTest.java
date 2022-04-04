package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.DiscoverConfigService;
import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.SystemOpsService;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.ClientModel;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.lang.reflect.Field;
import java.util.Collection;
import org.junit.jupiter.api.AfterAll;
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
 * Created by justin.xu on 04/2022.
 *
 * @since 1.8
 */
@Disabled
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
public class DiscoverRemoteServiceTest {

    @Autowired
    private SystemOpsService discoverDevOpsService;

    @Autowired
    private DiscoverConfigService discoverConfigService;

    @MockBean(name = "keyValueStorage")
    private KeyValueStorage keyValueStorage;

    private static String expectedAppId = "1474264648684351490";
    private static String expectedEnv = "0";

    private static RedisClient redisClient =
        RedisClient.create(RedisURI.Builder.redis("localhost", 6379).withPassword("8eSf4M97VLhP6hq8").build());

    @AfterAll
    private static void shutdown() {
        if (null != redisClient) {
            redisClient.shutdown();
        }
        InitializationHelper.destroy();
    }

    private static boolean isInit = false;
    /**
     * 每个测试前初始化.
     */
    @BeforeEach
    public void before() throws IllegalAccessException, JsonProcessingException {
        if (!isInit) {
            MetaManager metaManager = new StorageMetaManager(new ClientModel());

            DefaultCacheExecutor cacheExecutor = new DefaultCacheExecutor();

            Collection<Field> fields = ReflectionUtils.printAllMembers(cacheExecutor);
            ReflectionUtils.reflectionFieldValue(fields, "redisClient", cacheExecutor, redisClient);
            cacheExecutor.init();

            Collection<Field> cacheFields = ReflectionUtils.printAllMembers(metaManager);
            ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", metaManager,
                cacheExecutor);
            ReflectionUtils.reflectionFieldValue(cacheFields, "syncExecutor", metaManager,
                MetaInitialization.getInstance().getEntityClassSyncExecutor());
            ReflectionUtils.reflectionFieldValue(cacheFields, "asyncDispatcher", metaManager,
                CommonInitialization.getInstance().getRunner());

            Collection<Field> discoverFields = ReflectionUtils.printAllMembers(discoverDevOpsService);
            ReflectionUtils.reflectionFieldValue(discoverFields, "metaManager", discoverDevOpsService, metaManager);

            Collection<Field> configFields = ReflectionUtils.printAllMembers(discoverConfigService);
            ReflectionUtils.reflectionFieldValue(configFields, "metaManager", discoverConfigService, metaManager);

            isInit = true;
        }
    }

    @Test
    public void test() throws IllegalAccessException, InterruptedException {
        Thread.sleep(10000_000);
    }
}
