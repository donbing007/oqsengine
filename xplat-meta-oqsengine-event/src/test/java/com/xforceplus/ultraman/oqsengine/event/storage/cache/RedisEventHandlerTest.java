package com.xforceplus.ultraman.oqsengine.event.storage.cache;

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

        cacheEventHandler = redisEventHandler(redisClient, cacheWorker,
                new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false));
        cacheEventHandler.init();
    }

    @After
    public void after() {
        clearRedis();
    }

    @Test
    public void onEventCreateTest() {
        long expectedSize = 10;
        long expectedTxId = 1;
        for (long i = 0; i < 10; i ++) {
            Event<BuildPayload> payloadEvent = eventBuildGenerator(expectedTxId, i, i);
            cacheEventHandler.onEventCreate(payloadEvent);
        }
        Assert.assertTrue(checkWithExpire(expectedTxId, expectedSize, 1000));
    }


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
        int expectedSize = 10;
        for (int i = 0; i < threads; i++) {
            final int pos = i;
            handler[i] = new Thread(() -> {supply(pos * expectedSize, expectedSize);});
        }

        for (int i = 0; i < threads; i++) {
            handler[i].start();
        }

        Thread.sleep(3_000);

        for (Map.Entry<Long, AtomicInteger> entry : expectSizeByTxId.entrySet()) {
            Assert.assertTrue(checkWithExpire(entry.getKey(), entry.getValue().get(), 100));
        }
    }

    private String TX_EXPIRE_ZSORT_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.zsort";
    private String TX_EXPIRE_HASH_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.hash";
    @Test
    public void onEventBegin() {
        long txId = Long.MAX_VALUE - 1;

        long startTime = System.currentTimeMillis();
        int testSize = 10;


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
        return new RedisEventHandler(redisClient, cacheWorker, objectMapper);
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

    private Event<BuildPayload> eventBuildGenerator(long txId, long number, long id) {
        return new ActualEvent(ENTITY_BUILD, new BuildPayload(txId, number, Entity.Builder.anEntity().withId(id).build()));
    }

    private Event<ReplacePayload> eventReplaceGenerator(long txId, long number, long id) {
        return new ActualEvent(ENTITY_REPLACE, new ReplacePayload(txId, number
                , Entity.Builder.anEntity().withId(id).build(), Entity.Builder.anEntity().withId(id).build()));
    }

    private Event<DeletePayload> eventDeleteGenerator(long txId, long number, long id) {
        return new ActualEvent(ENTITY_DELETE, new DeletePayload(txId, number, Entity.Builder.anEntity().withId(id).build()));
    }

    private Event<BeginPayload> eventBeginGenerator(long txId) {
        return new ActualEvent(TX_BEGIN, new BeginPayload(txId, TX_BEGIN.name()));
    }

    private Event<CommitPayload> eventCommitGenerator(long txId, long number) {
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
}
