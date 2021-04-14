package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.gzip.ZipUtils;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static com.xforceplus.ultraman.oqsengine.event.EventType.*;

/**
 * desc :
 * name : RedisEventHandlerTest
 *
 * @author : xujia
 * date : 2021/4/9
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class RedisEventHandlerTest extends MiddleWare {

    final Logger logger = LoggerFactory.getLogger(RedisEventHandlerTest.class);

    private RedisEventHandler cacheEventHandler;

    private ObjectMapper objectMapper;

    private List<CachePayload> expectedCachePayloads = new ArrayList<>();

    private Method m;

    @BeforeClass
    public static void beforeClass() {
        initRedis();
    }

    @AfterClass
    public static void afterClass() {
        closeRedis();
    }

    @Before
    public void before() throws Exception {

        objectMapper = new ObjectMapper();

        cacheEventHandler = redisEventHandler(redisClient, objectMapper);
        cacheEventHandler.init();

        m = cacheEventHandler.getClass()
                .getDeclaredMethod("storage", new Class[]{CachePayload.class});
        m.setAccessible(true);
    }

    @After
    public void after() {
        clearRedis();
        expectedCachePayloads.clear();
    }

    private static final int testSize = 10;
    private static final int expire = 10;
    /**
     * 测试单线程写入10条数据是否符合预期
     */
    @Test
    public void onEventCreate() {
        long expectedTxId = 1;
        for (int i = 0; i < testSize; i ++) {
            cacheEventHandler.create(expectedTxId, i, randomEntityBuild(i, 1));
        }
        Assert.assertTrue(checkWithExpire(expectedTxId, testSize, 1000));
    }


    /**
     * 测试多线程并发CUD对于多个TXID的写入
     */
    private List<Integer> expectedTxId = Arrays.asList(3, 5, 20);
    private Map<Long, AtomicInteger> expectSizeByTxId = expectSizeByTxId();

    private List<RandomTXEventType> randomTx = Arrays.asList(
            new RandomTXEventType(expectedTxId.get(0), ENTITY_BUILD), new RandomTXEventType(expectedTxId.get(0), ENTITY_REPLACE), new RandomTXEventType(expectedTxId.get(0), ENTITY_DELETE),
            new RandomTXEventType(expectedTxId.get(1), ENTITY_BUILD), new RandomTXEventType(expectedTxId.get(1), ENTITY_REPLACE), new RandomTXEventType(expectedTxId.get(1), ENTITY_DELETE),
            new RandomTXEventType(expectedTxId.get(2), ENTITY_BUILD), new RandomTXEventType(expectedTxId.get(2), ENTITY_REPLACE), new RandomTXEventType(expectedTxId.get(2), ENTITY_DELETE));

    @Test
    public void onEventEntityOperationMultiThread() throws InterruptedException {
        int threads = 3;
        Thread[] handler = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            final int pos = i;
            handler[i] = new Thread(() -> {
                supply(pos * (int)testSize, (int)testSize);
            });
        }

        for (int i = 0; i < threads; i++) {
            handler[i].start();
        }

        Thread.sleep(3_000);

        for (Map.Entry<Long, AtomicInteger> entry : expectSizeByTxId.entrySet()) {
            Assert.assertTrue(checkWithExpire(entry.getKey(), entry.getValue().get(), 100));
        }
    }

    @Test
    public void storageTest() throws Exception {
        testAssertStorage(ENTITY_BUILD, 1, Long.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        testAssertStorage(ENTITY_BUILD, 1, Long.MAX_VALUE - 2, Integer.MAX_VALUE - 2);
        testAssertStorage(ENTITY_REPLACE, 1, Long.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        testAssertStorage(ENTITY_REPLACE, 1, Long.MAX_VALUE - 2, Integer.MAX_VALUE - 2);
        testAssertStorage(ENTITY_DELETE, 1, Long.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
        testAssertStorage(ENTITY_DELETE, 1, Long.MAX_VALUE - 2, Integer.MAX_VALUE - 2);
    }

    /**
     * 测试onEventCommit
     */
    @Test
    public void onEventCommit() throws Exception {
        //  测试正常写入10条数据然后提交
        long expectTx = 1;
        initBeginWithBuild(expectTx);
        cacheEventHandler.commit(expectTx, testSize);

        Collection<String> collection = cacheEventHandler.eventsQuery(expectTx, null, null, null);
        Assert.assertTrue(collection.size() > 0);

        Thread.sleep((expire + 1) * 1000);

        collection = cacheEventHandler.eventsQuery(expectTx, null, null, null);
        Assert.assertTrue(null == collection || collection.isEmpty());
    }


    @Test
    public void testSerial() throws Exception {
        CachePayload res =
                CacheEventHelper.toCachePayload(ENTITY_BUILD, 1, 2, randomEntityBuild(3, 1), null);

        String result = ZipUtils.zip(objectMapper.writeValueAsString(res));

        logger.info(result);

        CachePayload c = objectMapper.readValue(ZipUtils.unzip(result), CachePayload.class);

        assertCachePayload(res, c);
    }

    /**
     * 测试查询
     */
    @Test
    public void query() throws Exception {

        for (int i = 0; i < testSize; i ++) {
            initBeginWithBuild(i);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }

        for (CachePayload v : expectedCachePayloads) {
            //  这里传入2个-1是为了测试判断条件是否进入eventType check
            Collection<String> ret = cacheEventHandler.eventsQuery(v.getTxId(), -1L, -1, null);
            Assert.assertEquals(testSize, ret.size());

            storageWithQueryAssert(v);
        }
    }

    private void testAssertStorage(EventType eventType, long txId, long number, int version) throws Exception {
        CachePayload cachePayload;
        switch (eventType) {
            case ENTITY_BUILD:
            case ENTITY_DELETE:
                cachePayload = CacheEventHelper.toCachePayload(eventType, txId, number, randomEntityBuild(number, version), null);
                Assert.assertTrue((boolean) m.invoke(cacheEventHandler, cachePayload));
                storageWithQueryAssert(cachePayload);
                break;
            case ENTITY_REPLACE:
                cachePayload = CacheEventHelper.toCachePayload(eventType, txId, number, randomEntityBuild(number, version), randomEntityBuild(number, version - 1));
                Assert.assertTrue((boolean) m.invoke(cacheEventHandler, cachePayload));
                storageWithQueryAssert(cachePayload);
                break;
        }
    }

    private void storageWithQueryAssert(CachePayload cachePayload) throws Exception {
        Collection<String> ret = cacheEventHandler.eventsQuery(cachePayload.getTxId(), cachePayload.getNumber(), cachePayload.getVersion(), cachePayload.getEventType().getValue());
        Assert.assertEquals(1, ret.size());


        Iterator<String> it = ret.iterator();
        Assert.assertTrue(it.hasNext());

        assertCachePayload(cachePayload, objectMapper.readValue(ZipUtils.unzip(it.next()), CachePayload.class));
    }

    private void assertCachePayload(CachePayload expected, CachePayload actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getEventType(), actual.getEventType());
        Assert.assertEquals(expected.getNumber(), actual.getNumber());
        Assert.assertEquals(expected.getTime(), actual.getTime());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
        Assert.assertEquals(expected.getTxId(), actual.getTxId());
        Assert.assertEquals(expected.getFieldValueMapping().size(), actual.getFieldValueMapping().size());

        for (Map.Entry<Long, String> e : expected.getFieldValueMapping().entrySet()) {
            String v = actual.getFieldValueMapping().remove(e.getKey());
            Assert.assertEquals(e.getValue(), v);
        }

        if (null == expected.getOldFieldValueMapping()) {
            Assert.assertNull(actual.getOldFieldValueMapping());
        } else {
            Assert.assertEquals(expected.getOldFieldValueMapping().size(), actual.getOldFieldValueMapping().size());

            for (Map.Entry<Long, String> e : expected.getOldFieldValueMapping().entrySet()) {
                String v = actual.getOldFieldValueMapping().remove(e.getKey());
                Assert.assertEquals(e.getValue(), v);
            }
        }
    }

    private void initBeginWithBuild(long txId) throws Exception {
        for (int j = 0; j < testSize; j++) {
            buildOne(txId, j);
        }
    }

    private void buildOne(long txId, int current) throws Exception {
        IEntity entity = randomEntityBuild(current, current);
        CachePayload cachePayload = CacheEventHelper.toCachePayload(ENTITY_BUILD, txId, current, entity, null);

        boolean result = (boolean) m.invoke(cacheEventHandler, cachePayload);
        Assert.assertTrue(result);
        expectedCachePayloads.add(cachePayload);
    }

    private Map<Long, AtomicInteger> expectSizeByTxId() {
        Map<Long, AtomicInteger> expectSizeByTxId = new ConcurrentHashMap<>();
        expectedTxId.forEach(tx -> expectSizeByTxId.put(tx.longValue(), new AtomicInteger()));
        return expectSizeByTxId;
    }

    private void supply(int starter, int expectSize) {
        for (int i = starter; i < starter + expectSize; i ++) {
            int current = i % randomTx.size();
            RandomTXEventType r = randomTx.get(current);

            expectSizeByTxId.get(r.txId).incrementAndGet();

            switch (r.eventType) {
                case ENTITY_BUILD :
                    cacheEventHandler.create(r.txId, i, randomEntityBuild(i, 1));
                    break;
                case ENTITY_REPLACE :
                    cacheEventHandler.replace(r.txId, i, randomEntityBuild(i, 2), randomEntityBuild(i, 1));
                    break;
                case ENTITY_DELETE :
                    cacheEventHandler.delete(r.txId, i, randomEntityBuild(i, 2));
                    break;
            }
        }
    }

    private RedisEventHandler redisEventHandler(RedisClient redisClient, ObjectMapper objectMapper) {
        return new RedisEventHandler(redisClient,  objectMapper, 10);
    }

    private boolean checkWithExpire(long txId, long expected, long duration) {
        int currentLoop = 0;
        Collection<String> collection = null;
        while(currentLoop < duration) {
            collection =
                    cacheEventHandler.eventsQuery(txId, null, null, null);
            if (collection.size() == expected) {
                logger.info("after loops : [{}], check finished!", currentLoop);
                return true;
            }
            currentLoop ++;
            // 进行短暂的休眠.
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        logger.info("after loops timeout, check error broken : [{}-{}] !", collection == null ? 0 : collection.size(), expected);
        return false;
    }

    public static CachePayload cacheEventGenerator(EventType eventType,long txId, int number, long id, int version) {
        return CacheEventHelper.toCachePayload(eventType, txId, number, randomEntityBuild(id, version), null);
    }


    private static final int randomPlusNumber = 13149267;
    private static IEntity randomEntityBuild(long id, int version) {
        return Entity.Builder.anEntity()
                .withId(id)
                .withVersion(version)
                .withEntityValue(
                        EntityValue.build()
                                .addValue(new LongValue(genEntity(id, FieldType.LONG), id))
                                .addValue(new StringValue(genEntity(id + randomPlusNumber, FieldType.STRING), id + randomPlusNumber + ""))
                ).build();
    }


    private static IEntityField genEntity(long id, FieldType fieldType) {
        return EntityField.Builder.anEntityField()
                                .withId(id)
                                .withCnName("id" + "_name")
                                .withFieldType(fieldType)
                                .withConfig(
                                        FieldConfig.Builder.aFieldConfig().build()
                                ).build();
    }

    private static class RandomTXEventType {
        private long txId;
        private EventType eventType;

        public RandomTXEventType(long txId, EventType eventType) {
            this.txId = txId;
            this.eventType = eventType;
        }
    }
}
