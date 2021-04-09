package com.xforceplus.ultraman.oqsengine.event.storage.cache;

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
import io.lettuce.core.RedisClient;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

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
        cacheEventHandler = redisEventHandler(redisClient, cacheWorker);
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
        for (long i = 0; i <= 10; i ++) {
            Event<BuildPayload> payloadEvent = eventBuildGenerator(expectedTxId, i, i);
            cacheEventHandler.onEventCreate(payloadEvent);
        }
        checkWithExpire(expectedTxId, expectedSize, 100);

    }

    private RedisEventHandler redisEventHandler(RedisClient redisClient, ExecutorService cacheWorker) {
        return new RedisEventHandler(redisClient, cacheWorker);
    }

    private boolean checkWithExpire(long txId, long expired, long duration) {
        int currentLoop = 0;
        while(currentLoop < duration) {
            Collection<String> collection =
                    cacheEventHandler.eventsQuery(txId, null, null, null);
            if (collection.size() == expired) {
                return true;
            }
            currentLoop ++;
            // 进行短暂的休眠.
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }

        return false;
    }

    private Event<BuildPayload> eventBuildGenerator(long txId, long number, long id) {
        return new ActualEvent(ENTITY_BUILD, new BuildPayload(txId, number, Entity.Builder.anEntity().withId(id).build()));
    }




    private Event eventGenerator(EventType eventType, long txId, long number) {
        ActualEvent actualEvent = null;
        switch (eventType) {
            case ENTITY_BUILD :
                actualEvent = new ActualEvent(ENTITY_BUILD, new BuildPayload(txId, number, new Entity()));
                break;
            case ENTITY_REPLACE :
                actualEvent = new ActualEvent(ENTITY_REPLACE, new ReplacePayload(txId, number, new Entity(), new Entity()));
                break;
            case ENTITY_DELETE :
                actualEvent = new ActualEvent(ENTITY_DELETE, new DeletePayload(txId, number, new Entity()));
                break;
            case TX_BEGIN :
                actualEvent = new ActualEvent(TX_BEGIN, new BeginPayload(txId, TX_BEGIN.name()));
                break;
            case TX_COMMITED :
                actualEvent = new ActualEvent(TX_COMMITED, new CommitPayload(txId, 1, TX_COMMITED.name(), false, number));
                break;
        }
        return actualEvent;
    }
}
