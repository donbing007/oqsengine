package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private ExecutorService cacheWorker;

    private ObjectMapper objectMapper;

    private Map<Long, List<QueryCondition>> expectedTxValueMap = new HashMap<>();

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

        cacheWorker = Executors.newFixedThreadPool(10);

        objectMapper = new ObjectMapper();

        cacheEventHandler = redisEventHandler(redisClient, cacheWorker, objectMapper);
        cacheEventHandler.init();
    }

    @After
    public void after() {
        clearRedis();
        expectedTxValueMap.clear();
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
            Event<CachePayload> payloadEvent = cacheEventGenerator(ENTITY_BUILD, expectedTxId, i, i);
            cacheEventHandler.onEventCreate(payloadEvent);
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

    /**
     * 测试onEventCommit
     */
    @Test
    public void onEventCommit() throws JsonProcessingException, InterruptedException {
        //  测试正常写入10条数据然后提交
        long expectTx = 1;
        initBeginWithBuild(expectTx);
        cacheEventHandler.onEventCommit(expectTx, testSize);

        Collection<String> collection = cacheEventHandler.eventsQuery(expectTx, null, null, null);
        Assert.assertTrue(collection.size() > 0);

        Thread.sleep((expire + 1) * 1000);

        collection = cacheEventHandler.eventsQuery(expectTx, null, null, null);
        Assert.assertTrue(null == collection || collection.isEmpty());
    }


    @Test
    public void testSerial() throws JsonProcessingException {
        Event<CachePayload> res = cacheEventGenerator(ENTITY_BUILD, 1L, 2, 3L);

        String result = objectMapper.writeValueAsString(res);

        logger.info(result);
    }

    /**
     * 测试查询
     */
    @Test
    public void query() throws JsonProcessingException {

        for (int i = 0; i < testSize; i ++) {
            initBeginWithBuild(i);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }

        for (Map.Entry<Long, List<QueryCondition>> v : expectedTxValueMap.entrySet()) {
            //  这里传入2个-1是为了测试判断条件是否进入eventType check
            Collection<String> ret = cacheEventHandler.eventsQuery(v.getKey(), -1L, -1, null);
            Assert.assertEquals(testSize, ret.size());

            for (QueryCondition q : v.getValue()) {
                Collection<String> result = cacheEventHandler.eventsQuery(v.getKey(), q.id, q.version, q.eventType);
                Assert.assertEquals(1, result.size());

                Iterator<String> it = result.iterator();
                Assert.assertTrue(it.hasNext());
                Assert.assertEquals(Base64.getEncoder().encodeToString(q.expectValue.getBytes()), it.next());
            }
        }
    }

    private void initBeginWithBuild(long txId) throws JsonProcessingException {
        for (int j = 0; j < testSize; j++) {
            buildOne(txId, j);
        }
    }

    private void buildOne(long txId, int current) throws JsonProcessingException {
        Event<CachePayload> event = cacheEventGenerator(ENTITY_BUILD, txId, current, current);

        String json = objectMapper.writeValueAsString(event);

        boolean result = cacheEventHandler.onEventCreate(event);
        Assert.assertTrue(result);
        expectedTxValueMap.computeIfAbsent(txId, t -> new ArrayList<>()).add(new QueryCondition((long) current, current, ENTITY_BUILD.getValue(), json));
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
                    cacheEventHandler.onEventCreate(cacheEventGenerator(ENTITY_BUILD, r.txId, i, i));
                    break;
                case ENTITY_REPLACE :
                    cacheEventHandler.onEventUpdate(cacheEventGenerator(ENTITY_REPLACE, r.txId, i, i));
                    break;
                case ENTITY_DELETE :
                    cacheEventHandler.onEventDelete(cacheEventGenerator(ENTITY_DELETE, r.txId, i, i));
                    break;
            }
        }
    }


    private RedisEventHandler redisEventHandler(RedisClient redisClient, ExecutorService cacheWorker, ObjectMapper objectMapper) {
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

    public static Event<CachePayload> cacheEventGenerator(EventType eventType,long txId, int number, long id) {
        return new ActualEvent(eventType, new CachePayload(txId, number, id, number, randomFixEntityValues(id)));
    }

    private static final int randomPlusNumber = 13149267;
    private static Map<IEntityField, Object> randomFixEntityValues(long id) {
        Map<IEntityField, Object> map = new HashMap<>();
        IEntityField entityFieldLong = genEntity(id, FieldType.LONG);
        map.put(entityFieldLong, new LongValue(entityFieldLong, id));

        IEntityField entityFieldString = genEntity(id + randomPlusNumber, FieldType.STRING);
        map.put(entityFieldString, new StringValue(entityFieldString, id + randomPlusNumber + ""));
        return map;
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

    private Event<BeginPayload> eventBeginGenerator(long txId) {
        return new ActualEvent(TX_BEGIN, new BeginPayload(txId, TX_BEGIN.name()));
    }

    private Event<CommitPayload> eventCommitGenerator(long txId, int number) {
        return new ActualEvent(TX_COMMITED, new CommitPayload(txId, 1, TX_COMMITED.name(), false, number));
    }

    private static class RandomTXEventType {
        private long txId;
        private EventType eventType;

        public RandomTXEventType(long txId, EventType eventType) {
            this.txId = txId;
            this.eventType = eventType;
        }
    }

    private static class QueryCondition {
        Long id;
        Integer version;
        Integer eventType;
        String expectValue;

        public QueryCondition(Long id, Integer version, Integer eventType, String expectValue) {
            this.id = id;
            this.version = version;
            this.eventType = eventType;
            this.expectValue = expectValue;
        }
    }
}
