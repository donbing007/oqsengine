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
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.lang.reflect.Field;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
@ActiveProfiles("discover")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Disabled("不是常用的测试,平时忽略.")
public class DiscoverDevOpsServiceTest extends AbstractContainerExtends {

    @Autowired
    private SystemOpsService discoverDevOpsService;

    @MockBean(name = "keyValueStorage")
    private KeyValueStorage keyValueStorage;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

//    // mockBean below need to delete
//    @MockBean(name = "longContinuousPartialOrderIdGenerator")
//    private LongIdGenerator longIdGenerator;
//
//    @MockBean(name = "longNoContinuousPartialOrderIdGenerator")
//    private LongIdGenerator longPartitionIdGenerator;
//
//    @MockBean
//    private EntitySearchService entitySearchService;
//
//    @MockBean
//    private TransactionManager transactionManager;
//
//    @MockBean
//    private IndexStorage indexStorage;
//
//    @MockBean
//    private MasterStorage masterStorage;
//
//    @MockBean
//    private TaskQueue taskQueue;
//
//    @MockBean
//    private TaskCoordinator taskCoordinator;

    private boolean waitForDebug = false;

    private static String expectedAppId = "discover-test";
    private static int expectedVersion = Integer.MAX_VALUE;

    /**
     * 每个测试前初始化.
     */
    @BeforeEach
    public void before() throws IllegalAccessException {
        MetaManager metaManager = new StorageMetaManager();

        Collection<Field> cacheFields = ReflectionUtils.printAllMembers(metaManager);
        ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", metaManager,
            MetaInitialization.getInstance().getCacheExecutor());
        ReflectionUtils.reflectionFieldValue(cacheFields, "syncExecutor", metaManager,
            MetaInitialization.getInstance().getEntityClassSyncExecutor());
        ReflectionUtils.reflectionFieldValue(cacheFields, "asyncDispatcher", metaManager,
            CommonInitialization.getInstance().getRunner());

        Collection<Field> discoverFields = ReflectionUtils.printAllMembers(discoverDevOpsService);
        ReflectionUtils.reflectionFieldValue(discoverFields, "metaManager", discoverDevOpsService, metaManager);
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
    }

    @Test
    public void test() throws InterruptedException, JsonProcessingException {
        MockedCache.entityClassStorageSave(expectedAppId, expectedVersion);
        if (waitForDebug) {
            Thread.sleep(10000_000);
        } else {
            Thread.sleep(5_000);
        }
    }
}
