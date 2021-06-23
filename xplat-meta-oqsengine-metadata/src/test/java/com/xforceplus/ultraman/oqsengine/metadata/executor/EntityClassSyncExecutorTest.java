package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : EntityClassSyncExecutorTest
 *
 * @author : xujia 2021/2/22
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class EntityClassSyncExecutorTest {
    private RedisClient redisClient;

    private DefaultCacheExecutor cacheExecutor;

    private EntityClassSyncExecutor entityClassSyncExecutor;

    private MockRequestHandler mockRequestHandler;

    private StorageMetaManager storageMetaManager;

    private ExecutorService executorService;


    @Before
    public void before() throws Exception {
        /*
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        /*
         * init cacheExecutor
         */
        cacheExecutor = new DefaultCacheExecutor();
        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        cacheExecutor.init();

        /*
         * init entityClassExecutor
         */
        entityClassSyncExecutor = new EntityClassSyncExecutor();
        ReflectionTestUtils.setField(entityClassSyncExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassSyncExecutor, "expireExecutor", new ExpireExecutor());
        ReflectionTestUtils.setField(entityClassSyncExecutor, "eventBus", new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                Assert.assertEquals(type, EventType.AUTO_FILL_UPGRADE);
            }

            @Override
            public void notify(Event event) {
                Assert.assertEquals(event.type(), EventType.AUTO_FILL_UPGRADE);
            }
        });

        entityClassSyncExecutor.setLoadPath("src/test/resources/local/");
        entityClassSyncExecutor.start();

        /*
         * init mockRequestHandler
         */
        mockRequestHandler = new MockRequestHandler();
        ReflectionTestUtils.setField(mockRequestHandler, "syncExecutor", entityClassSyncExecutor);

        /*
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));
        storageMetaManager = new StorageMetaManager();
        ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(storageMetaManager, "requestHandler", mockRequestHandler);
        ReflectionTestUtils.setField(storageMetaManager, "asyncDispatcher", executorService);
    }

    @Test
    public void dataImportTest() throws IOException {
        String defaultTestAppId = "5";
        String env = "0";
        int defaultTestVersion = 2;
        Boolean result = false;
        InputStream in = null;
        try {
            in = initInputStreamByResource(defaultTestAppId, defaultTestVersion, env);

            result = entityClassSyncExecutor.dataImport(defaultTestAppId, defaultTestVersion,
                EntityClassStorageHelper.initDataFromInputStream(defaultTestAppId, env, defaultTestVersion, in));
            Assert.assertTrue(result);
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (null != in) {
                in.close();
            }
        }

        Optional<IEntityClass> op =  storageMetaManager.load(1251658380868685825L);

        Assert.assertTrue(op.isPresent());

        //  重新导入老版本，结果为失败
        try {
            in = initInputStreamByResource(defaultTestAppId, defaultTestVersion, env);

            defaultTestVersion = 1;

            result = entityClassSyncExecutor.dataImport(defaultTestAppId, defaultTestVersion,
                EntityClassStorageHelper.initDataFromInputStream(defaultTestAppId, env, defaultTestVersion, in));
            Assert.assertFalse(result);
        } catch (Exception e) {
            Assert.fail();
        } finally {
            if (null != in) {
                in.close();
            }
        }
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
            entityClassSyncExecutor.sync(expectedAppId, expectedVersion, entityClassSyncRspProto);

        Assert.assertTrue(ret);

        int newVersion = expectedVersion + 1;
        ret =
            entityClassSyncExecutor.sync(expectedAppId, newVersion, entityClassSyncRspProto);

        Assert.assertTrue(ret);

        Thread.sleep(70_000);

        Method m0 = cacheExecutor.getClass()
            .getDeclaredMethod("getFromLocal", long.class, int.class);
        m0.setAccessible(true);

        Method m1 = cacheExecutor.getClass()
            .getDeclaredMethod("getOneFromRemote", long.class, int.class);
        m1.setAccessible(true);


        Assert.assertEquals(newVersion, cacheExecutor.version(expectedAppId));

        for (ExpectedEntityStorage e : expectedEntityStorageList) {
            /*
             * 本地缓存中已不存在
             */
            EntityClassStorage notExists =
                (EntityClassStorage) m0.invoke(cacheExecutor, new Object[] {e.getSelf(), expectedVersion});
            Assert.assertNull(notExists);

            /*
             * Remote缓存中也不存在
             */
            try {
                m1.invoke(cacheExecutor, e.getSelf(), expectedVersion);
            } catch (Exception ex) {
                //  ignore
            }

            /*
             * 当前活动的
             */
            EntityClassStorage exists =
                (EntityClassStorage) m1.invoke(cacheExecutor, new Object[] {e.getSelf(), newVersion});
            Assert.assertNotNull(exists);

            cacheExecutor.read(e.getSelf());

            exists =
                (EntityClassStorage) m0.invoke(cacheExecutor, new Object[] {e.getSelf(), newVersion});
            Assert.assertNotNull(exists);
        }
    }


    /**
     * 从resource目录中生成InputStream.
     */
    private InputStream initInputStreamByResource(String appId, Integer version, String env) {
        String path = String.format("/%s_%d_%s.json", appId, version, env);
        return EntityClassStorageHelper.class.getResourceAsStream(path);
    }

}
