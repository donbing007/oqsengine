package com.xforceplus.ulraman.oqsengine.metadata.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.*;

/**
 * desc :
 * name : CacheExecutorTest
 *
 * @author : xujia
 * date : 2021/2/16
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class CacheExecutorTest {
    private RedisClient redisClient;
    private CacheExecutor cacheExecutor;

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();

        ObjectMapper objectMapper = new ObjectMapper();
        cacheExecutor = new CacheExecutor();
        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        ReflectionTestUtils.setField(cacheExecutor, "objectMapper", objectMapper);
        cacheExecutor.init();
    }

    @After
    public void after() throws Exception {
        syncConnect.close();
        cacheExecutor.destroy();
        cacheExecutor = null;

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    @Test
    public void testPrepare() throws InterruptedException {
        boolean ret = cacheExecutor.prepare("1", 1);
        Assert.assertTrue(ret);

        ret = cacheExecutor.prepare("1", 0);
        Assert.assertFalse(ret);

        ret = cacheExecutor.endPrepare("1");
        Assert.assertTrue(ret);

        ret = cacheExecutor.resetVersion("1", 2);
        Assert.assertTrue(ret);

        ret = cacheExecutor.prepare("1", 1);
        Assert.assertFalse(ret);

        ret = cacheExecutor.prepare("1", 3);
        Assert.assertTrue(ret);

        ret = cacheExecutor.endPrepare("1");
        Assert.assertTrue(ret);
    }


    @Test
    public void testFullEntityClassStorageQuery() {
        prepareEntity("test", 5L, 1, 2L, Arrays.asList(10L, 20L), Arrays.asList(4L, 3L));
        prepareEntity("test", 10L, 1, 20L, Arrays.asList(20L), Arrays.asList(5L, 4L, 3L));
        prepareEntity("test", 20L, 1, null, null, Arrays.asList(10L, 5L, 4L, 3L));
        prepareEntity("test", 4L, 1, 5L, Arrays.asList(5L, 10L, 20L), null);
        prepareEntity("test", 3L, 1, 5L, Arrays.asList(5L, 10L, 20L), null);

        Map<Long, EntityClassStorage> map = cacheExecutor.read(5L);

        Assert.assertEquals(5, map.size());
    }

    private void prepareEntity(String appId, long entityId, int version, Long father, List<Long> ancestors, List<Long> children) {
        IEntityField[] entityFields = new IEntityField[2];
        entityFields[0] = entityFieldLong(entityId);
        entityFields[1] = entityFieldString(entityId + 1);

        Relation[] relations = new Relation[2];
        relations[0] = relationLong(entityId, entityId - 1);
        relations[1] = relationString(entityId, entityId - 2);

        EntityClassStorage entityClassStorage = new EntityClassStorage();
        entityClassStorage.setId(entityId);
        entityClassStorage.setName(entityId + "_name");
        entityClassStorage.setCode(entityId + "_code");
        entityClassStorage.setVersion(1);


        if (null != father) {
            entityClassStorage.setFatherId(father);
        }

        if (null != ancestors) {
            entityClassStorage.setAncestors(ancestors);
            entityClassStorage.setLevel(ancestors.size());
        } else {
            entityClassStorage.setLevel(0);
        }

        if (null != children) {
            entityClassStorage.setChildIds(children);
        }
        entityClassStorage.setFields(Arrays.asList(entityFields));
        entityClassStorage.setRelations(Arrays.asList(relations));

        //  set storage
        cacheExecutor.save(appId, version, Collections.singletonList(entityClassStorage));
    }


}
