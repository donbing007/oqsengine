package com.xforceplus.ultraman.oqsengine.metadata.integeration.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.handler.DefaultEntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.handler.EntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 请注意这个测试常关闭，只作为连接某个环境排查redis中错误使用.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/11
 * @since 1.8
 */
public class RemoteRedisTest {
    private final boolean isTestOpen = false;
    private RedisClient redisClient;
    private DefaultCacheExecutor cacheExecutor;

    private StorageMetaManager storageMetaManager;
    public EntityClassFormatHandler entityClassFormatHandler;

    @BeforeEach
    public void before() throws Exception {
        if (isTestOpen) {
            String redisIp = "localhost";
            int redisPort = 6379;
            redisClient =
                RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).withPassword("8eSf4M97VLhP6hq8").build());

            cacheExecutor = new DefaultCacheExecutor();

            ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
            cacheExecutor.init();

            entityClassFormatHandler = new DefaultEntityClassFormatHandler();
            ReflectionTestUtils.setField(entityClassFormatHandler, "cacheExecutor", cacheExecutor);

            storageMetaManager = new StorageMetaManager();
            ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
            ReflectionTestUtils.setField(storageMetaManager, "entityClassFormatHandler", entityClassFormatHandler);
        }
    }

    @AfterEach
    public void after() throws Exception {
        if (isTestOpen) {
            cacheExecutor.destroy();
            cacheExecutor = null;

            redisClient.shutdown();
            redisClient = null;
        }
    }

    @Test
    public void load() throws JsonProcessingException {
        if (isTestOpen) {
            Optional<IEntityClass> entityClassOptional = storageMetaManager.load(1422422995802726402L);
            Assertions.assertTrue(entityClassOptional.isPresent());
        }
    }

}
