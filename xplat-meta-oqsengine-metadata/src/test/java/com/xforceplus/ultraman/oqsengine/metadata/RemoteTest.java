package com.xforceplus.ultraman.oqsengine.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/11
 * @since 1.8
 */
public class RemoteTest {
    private boolean isTestOpen = false;
    private RedisClient redisClient;
    private DefaultCacheExecutor cacheExecutor;

    private StorageMetaManager storageMetaManager;

    @Before
    public void before() throws Exception {
        if (isTestOpen) {
            String redisIp = "localhost";
            int redisPort = 6379;
            redisClient =
                RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).withPassword("8eSf4M97VLhP6hq9").build());

            ObjectMapper objectMapper = new ObjectMapper();
            cacheExecutor = new DefaultCacheExecutor();

            ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
            ReflectionTestUtils.setField(cacheExecutor, "objectMapper", objectMapper);
            cacheExecutor.init();

            storageMetaManager = new StorageMetaManager();
            ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
        }
    }

    @After
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
            Optional<IEntityClass> entityClassOptional = storageMetaManager.load(1334095964616527874L);
            Assert.assertTrue(entityClassOptional.isPresent());
        }
    }

}
