package com.xforceplus.ulraman.oqsengine.metadata.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ulraman.oqsengine.metadata.mock.MockRequestHandler;
import com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassManagerExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.*;

/**
 * desc :
 * name : EntityClassSyncExecutorTest
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class EntityClassSyncExecutorTest {
    private RedisClient redisClient;

    private CacheExecutor cacheExecutor;

    private EntityClassSyncExecutor entityClassSyncExecutor;

    private MockRequestHandler mockRequestHandler;

    private EntityClassManagerExecutor entityClassManagerExecutor;

    private ExecutorService executorService;


    @Before
    public void before() throws Exception {
        /**
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        /**
         * init cacheExecutor
         */
        cacheExecutor = new CacheExecutor();
        ObjectMapper objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        ReflectionTestUtils.setField(cacheExecutor, "objectMapper", objectMapper);
        cacheExecutor.init();

        /**
         * init entityClassExecutor
         */
        entityClassSyncExecutor = new EntityClassSyncExecutor();
        ReflectionTestUtils.setField(entityClassSyncExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassSyncExecutor, "expireExecutor", new ExpireExecutor());

        entityClassSyncExecutor.start();

        /**
         * init mockRequestHandler
         */
        mockRequestHandler = new MockRequestHandler();
        ReflectionTestUtils.setField(mockRequestHandler, "syncExecutor", entityClassSyncExecutor);

        /**
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));
        entityClassManagerExecutor = new EntityClassManagerExecutor();
        ReflectionTestUtils.setField(entityClassManagerExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassManagerExecutor, "requestHandler", mockRequestHandler);
        ReflectionTestUtils.setField(entityClassManagerExecutor, "asyncDispatcher", executorService);
    }

    @Test
    public void syncTest() throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, JsonProcessingException {
        String expectedAppId = "testLoad";
        int expectedVersion = 1;
        long expectedId = System.currentTimeMillis() + 3600_000;

        List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorageList = mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncRspProto entityClassSyncRspProto =
                entityClassSyncRspProtoGenerator(expectedEntityStorageList);

        boolean ret =
                entityClassSyncExecutor.sync(expectedAppId, expectedVersion, entityClassSyncRspProto);

        Assert.assertTrue(ret);

        int newVersion = expectedVersion + 1;
        ret =
            entityClassSyncExecutor.sync(expectedAppId, newVersion, entityClassSyncRspProto);

        Assert.assertTrue(ret);

        Thread.sleep(70_000);

        Method m0 = cacheExecutor.getClass()
                .getDeclaredMethod("getFromLocal", new Class[]{long.class, int.class});
        m0.setAccessible(true);

        Method m1 = cacheExecutor.getClass()
                .getDeclaredMethod("getOneFromRemote", new Class[]{long.class, int.class});
        m1.setAccessible(true);


        Assert.assertEquals(newVersion, cacheExecutor.version(expectedAppId));

        for (EntityClassStorageBuilder.ExpectedEntityStorage e : expectedEntityStorageList) {
            /**
             * 本地缓存中已不存在
             */
            EntityClassStorage notExists =
                    (EntityClassStorage) m0.invoke(cacheExecutor, new Object[]{e.getSelf(), expectedVersion});
            Assert.assertNull(notExists);

            /**
             * Remote缓存中也不存在
             */
            try {
                m1.invoke(cacheExecutor, new Object[]{e.getSelf(), expectedVersion});
            } catch (Exception ex) {
                //  ignore
            }

            /**
             * 当前活动的
             */
            EntityClassStorage exists = (EntityClassStorage) m1.invoke(cacheExecutor, new Object[]{e.getSelf(), newVersion});
            Assert.assertNotNull(exists);

            cacheExecutor.read(e.getSelf());

            exists =
                    (EntityClassStorage) m0.invoke(cacheExecutor, new Object[]{e.getSelf(), newVersion});
            Assert.assertNotNull(exists);
        }
    }

}
