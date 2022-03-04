package com.xforceplus.ultraman.oqsengine.metadata.integeration.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.ClientModel;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 请注意这个测试常关闭，只作为连接某个环境排查redis中错误使用.
 * 注意:这个测试需要链接某个环境中的真实数据，以测试load功能是否正常.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/11
 * @since 1.8
 */
@Disabled
public class RemoteRedisTest {
    private RedisClient redisClient;
    private DefaultCacheExecutor cacheExecutor;

    private StorageMetaManager storageMetaManager;


    private static final String password = "8eSf4M97VLhP6hq8";
    private static final String ip = "localhost";
    private static final int port = 6379;

    private static final long entityClassId = 1422422995802726402L;

    @BeforeEach
    public void before() throws Exception {
        redisClient =
            RedisClient.create(RedisURI.Builder.redis(ip, port).withPassword(password).build());

        cacheExecutor = new DefaultCacheExecutor();

        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        cacheExecutor.init();


        storageMetaManager = new StorageMetaManager(new ClientModel());
        ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
    }

    @AfterEach
    public void after() throws Exception {
        cacheExecutor.destroy();
        cacheExecutor = null;

        redisClient.shutdown();
        redisClient = null;
    }

    @Test
    public void load() throws JsonProcessingException {
        Optional<IEntityClass> entityClassOptional = storageMetaManager.load(entityClassId, "");
        Assertions.assertTrue(entityClassOptional.isPresent());
    }
}
