package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
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
import static com.xforceplus.ultraman.oqsengine.event.EventType.TX_COMMITED;

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

        objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        cacheEventHandler = redisEventHandler(redisClient, cacheWorker, objectMapper);
        cacheEventHandler.init();
    }

    @After
    public void after() {
        clearRedis();
        expectedTxValueMap.clear();
    }

    private static final int testSize = 10;

    /**
     * 测试单线程写入10条数据是否符合预期
     */
    @Test
    public void onEventCreate() {
        long expectedTxId = 1;
        for (int i = 0; i < testSize; i ++) {
            Event<BuildPayload> payloadEvent = eventBuildGenerator(expectedTxId, i, i);
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
     * 测试onEventBegin
     */
    private String TX_EXPIRE_ZSORT_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.zsort";
    private String TX_EXPIRE_HASH_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.hash";
    @Test
    public void onEventBegin() {
        long txId = Long.MAX_VALUE - 1;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < testSize; i++) {
            Event<BeginPayload> payloadEvent = eventBeginGenerator(txId--);
            boolean result = cacheEventHandler.onEventBegin(payloadEvent);
            Assert.assertTrue(result);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        long endTime = System.currentTimeMillis() + 1;
        Range<Long> r = Range.create(startTime, endTime);
        List<String> needCleans = syncCommands.zrangebyscore(TX_EXPIRE_ZSORT_KEY, r);
        Assert.assertTrue(null != needCleans && needCleans.size() == testSize);

        Map<String, String> result = syncCommands.hgetall(TX_EXPIRE_HASH_KEY);
        Assert.assertEquals(testSize, result.size());
        for (int i = 1; i <= testSize; i++) {
            String v = result.get(String.format("%s", txId + i));
            Assert.assertNotNull(v);
            Assert.assertTrue(Long.parseLong(v) > startTime && Long.parseLong(v) < endTime);
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
        cacheEventHandler.onEventCommit(eventCommitGenerator(expectTx, testSize));
        consumerCheck(expectTx + "");

        //  测试写入10条数据、commit的maxOpNumber为11时，等待10毫秒写入第10条数据然后提交
        final long otTx = 2;
        initBeginWithBuild(otTx);
        Thread onCommitThread = new Thread(() -> {
            cacheEventHandler.onEventCommit(eventCommitGenerator(otTx, testSize + 1));

        });
        Thread buildOneThread = new Thread(() -> {
            try {
                buildOne(otTx, testSize);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        //  确保commit线程产生exception并执行recover
        onCommitThread.start();
        onCommitThread.join();

        //  将最后一个补齐
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        buildOneThread.start();
        buildOneThread.join();
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));

        consumerCheck(otTx + "");
    }


    /**
     * 测试删除
     */
    private String CUD_PAYLOAD_HASH_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.event.payload";
    @Test
    public void eventClean() {
        Long[] expectedDeleteTx = new Long[2];
        expectedDeleteTx[0] = Long.MAX_VALUE - 1;
        expectedDeleteTx[1] = Long.MAX_VALUE - 2;

        long start = System.currentTimeMillis();
        long end = 0;
        int existStart = 0;
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));

        for (int i = 0; i < testSize; i ++) {
            long txId = 0;
            if (i < expectedDeleteTx.length) {
                txId = expectedDeleteTx[i];
            } else {
                txId = i;
            }
            Event<BeginPayload> payloadEvent = eventBeginGenerator(txId);
            boolean result = cacheEventHandler.onEventBegin(payloadEvent);
            Assert.assertTrue(result);

            if (i == expectedDeleteTx.length) {
                end = payloadEvent.time() - 5;
                existStart = i;
            }

            for (int j = 0; j < testSize; j++) {
                result = cacheEventHandler.onEventCreate(eventBuildGenerator(txId, j, j));
                Assert.assertTrue(result);
            }

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }

        int removes = cacheEventHandler.eventCleanByRange(start, end);
        Assert.assertEquals(expectedDeleteTx.length, removes);

        Range<Long> r = Range.create(start, end);
        List<String> ret = syncCommands.zrangebyscore(TX_EXPIRE_ZSORT_KEY, r);
        Assert.assertEquals(0, ret.size());

        for (int i = 0; i < expectedDeleteTx.length; i++) {
            cleanNotExistAssert(expectedDeleteTx[i] + "");
        }

        // 将existStart删除，测试cleanByTxId
        cacheEventHandler.eventCleanByTxId(existStart);

        for (int i = existStart; i < testSize; i ++) {
            String key = i + "";
            if (i == existStart) {
                cleanNotExistAssert(key);
            } else {
                String result = syncCommands.hget(TX_EXPIRE_HASH_KEY, key);
                Assert.assertNotNull(result);

                Map<String, String> stringMap = syncCommands.hgetall(String.format("%s.%s", CUD_PAYLOAD_HASH_KEY_PREFIX, key));
                Assert.assertEquals(stringMap.size(), testSize);
            }
        }
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
                Assert.assertEquals(q.expectValue, it.next());
            }
        }
    }

    private String STREAM_TX_ID = "com.xforceplus.ultraman.oqsengine.event.stream.tx";
    private void consumerCheck(String txIdStr) {
        List<StreamMessage<String, String>> streamSmsSend = syncCommands.xread(XReadArgs.StreamOffset.from(STREAM_TX_ID, "0"));

        boolean find = false;
        for (StreamMessage<String, String> message : streamSmsSend) {
            String messageValue = message.getBody().get(txIdStr);
            if (null != messageValue) {
                find = true;
                break;
            }
        }
        Assert.assertTrue(find);

    }

    private void initBeginWithBuild(long txId) throws JsonProcessingException {
        Event<BeginPayload> payloadEvent = eventBeginGenerator(txId);
        boolean result = cacheEventHandler.onEventBegin(payloadEvent);
        Assert.assertTrue(result);

        for (int j = 0; j < testSize; j++) {
            buildOne(txId, j);
        }
    }

    private void buildOne(long txId, int current) throws JsonProcessingException {
        Event<BuildPayload> event = eventBuildGenerator(txId, current, current);

        String json = objectMapper.writeValueAsString(event);

        boolean result = cacheEventHandler.onEventCreate(event);
        Assert.assertTrue(result);
        expectedTxValueMap.computeIfAbsent(txId, t -> new ArrayList<>()).add(new QueryCondition((long) current, current, ENTITY_BUILD.getValue(), json));
    }

    private void cleanNotExistAssert(String key) {
        String result = syncCommands.hget(TX_EXPIRE_HASH_KEY, key);
        Assert.assertNull(result);

        result = syncCommands.get(String.format("%s.%s", CUD_PAYLOAD_HASH_KEY_PREFIX, key));
        Assert.assertNull(result);
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
                    cacheEventHandler.onEventCreate(eventBuildGenerator(r.txId, i, i));
                    break;
                case ENTITY_REPLACE :
                    cacheEventHandler.onEventUpdate(eventReplaceGenerator(r.txId, i, i));
                    break;
                case ENTITY_DELETE :
                    cacheEventHandler.onEventDelete(eventDeleteGenerator(r.txId, i, i));
                    break;
            }
        }
    }


    private RedisEventHandler redisEventHandler(RedisClient redisClient, ExecutorService cacheWorker, ObjectMapper objectMapper) {
        return new RedisEventHandler(redisClient, cacheWorker, objectMapper, 0);
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

    private Event<BuildPayload> eventBuildGenerator(long txId, int number, long id) {
        return new ActualEvent(ENTITY_BUILD, new BuildPayload(txId, number, Entity.Builder.anEntity().withId(id).withVersion(number).build()));
    }

    private Event<ReplacePayload> eventReplaceGenerator(long txId, int number, long id) {
        return new ActualEvent(ENTITY_REPLACE, new ReplacePayload(txId, number
                , Entity.Builder.anEntity().withId(id).build(), Entity.Builder.anEntity().withId(id).withVersion(number).build()));
    }

    private Event<DeletePayload> eventDeleteGenerator(long txId, int number, long id) {
        return new ActualEvent(ENTITY_DELETE, new DeletePayload(txId, number, Entity.Builder.anEntity().withId(id).withVersion(number).build()));
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
