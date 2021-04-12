package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.xforceplus.ultraman.oqsengine.event.*;
import com.xforceplus.ultraman.oqsengine.event.payload.cache.CachePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.RollbackPayload;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xforceplus.ultraman.oqsengine.event.EventType.*;

/**
 * desc :
 * name : CacheEventServiceTest
 *
 * @author : xujia
 * date : 2021/4/9
 * @since : 1.8
 */
public class CacheEventServiceTest {

    private CacheEventService eventService;
    private ICacheEventHandler cacheEventHandler;

    private DefaultEventBus eventBus;

    private ExecutorService busWorker;

    private static long expectedSize = 0;
    private static long totalSend = 0;

    public static Map<Long, List<Event>> resultMap = new ConcurrentHashMap();

    @Before
    public void before() throws Exception {

        busWorker = Executors.newFixedThreadPool(10);
        eventBus = eventBus(busWorker);

        cacheEventHandler = new MockEventHandler();

        eventService = cacheEventService(eventBus, cacheEventHandler);

        initAll();
    }

    @After
    public void after() throws Exception {
        eventService.destroy();
        eventBus.destroy();

        busWorker.shutdown();
    }

    /**
     * 测试正常收发事件，目前只关注部分事件
     * @throws InterruptedException
     */
    @Test
    public void onEventWatchTest() throws InterruptedException {
        Map<Long, List<Event>> expectMap = prepare();

        Assert.assertTrue(expectedSize > 0);
        Assert.assertEquals(expectedSize, cacheEventHandler.queueSize());
        Assert.assertTrue(totalSend > cacheEventHandler.queueSize());

        for (Map.Entry<Long, List<Event>> entry : expectMap.entrySet()) {
            List<Event> events = resultMap.get(entry.getKey());
            Assert.assertNotNull(events);
            Assert.assertEquals(entry.getValue().size(), events.size());
        }
    }

    private Map<Long, List<Event>> prepare() throws InterruptedException {
        try {
            Map<Long, List<Event>> expectMap = new ConcurrentHashMap();
            for (long i = 1; i < 10; i++) {
                int count = 0;
                for (int j = 10; j > i; j--) {
                    count++;
                    totalSend ++;
                    EventType eventType = EventType.getInstance(j);
                    ActualEvent actualEvent;
                    switch (eventType) {
                        case ENTITY_BUILD :
                            actualEvent = new ActualEvent(ENTITY_BUILD, new BuildPayload(i, count, genEntity(i, count)));
                            eventBus.notify(actualEvent);
                            expectMap.computeIfAbsent(i, t -> new CopyOnWriteArrayList<>()).add(actualEvent);
                            expectedSize ++;
                            break;
                        case ENTITY_REPLACE :
                            actualEvent = new ActualEvent(ENTITY_REPLACE, new ReplacePayload(i, count, genEntity(i, count), genEntity(i, count - 1)));
                            eventBus.notify(actualEvent);
                            expectMap.computeIfAbsent(i, t -> new CopyOnWriteArrayList<>()).add(actualEvent);
                            expectedSize ++;
                            break;
                        case ENTITY_DELETE :
                            actualEvent = new ActualEvent(ENTITY_DELETE, new DeletePayload(i, count, genEntity(i, count)));
                            eventBus.notify(actualEvent);
                            expectMap.computeIfAbsent(i, t -> new CopyOnWriteArrayList<>()).add(actualEvent);
                            expectedSize ++;
                            break;
                        case TX_BEGIN :
                            actualEvent = new ActualEvent(TX_BEGIN, new BeginPayload(i, TX_BEGIN.name()));
                            eventBus.notify(actualEvent);
                            expectMap.computeIfAbsent(i, t -> new CopyOnWriteArrayList<>()).add(actualEvent);
                            expectedSize ++;
                            break;
                        case TX_COMMITED :
                            actualEvent = new ActualEvent(TX_COMMITED, new CommitPayload(i, i, TX_COMMITED.name(), false, count));
                            eventBus.notify(actualEvent);
                            expectMap.computeIfAbsent(i, t -> new CopyOnWriteArrayList<>()).add(actualEvent);
                            expectedSize ++;
                            break;
                        case UNKNOWN :
                            break;
                        case TX_PREPAREDNESS_COMMIT :
                            eventBus.notify(new ActualEvent(TX_PREPAREDNESS_COMMIT, new CommitPayload(i, i, TX_COMMITED.name(), false, count)));
                            break;
                        case TX_PREPAREDNESS_ROLLBACK :
                            eventBus.notify(new ActualEvent(TX_PREPAREDNESS_COMMIT, new RollbackPayload(i, count, TX_COMMITED.name())));
                            break;
                    }
                }


            }
            return expectMap;
        } finally {
            Thread.sleep(1_000);
        }
    }

    private IEntity genEntity(long id, int version) {
        return Entity.Builder.anEntity().withId(id).withVersion(version).withEntityValue(
                EntityValue.build().addValue(
                        new LongValue(EntityField.Builder.anEntityField()
                                            .withId(id)
                                            .withFieldType(FieldType.LONG)
                                            .withConfig(
                                                FieldConfig.Builder.aFieldConfig().build()
                                            ).build(),
                                        id))).build();
    }

    private void initAll() {
        eventBus.init();
        eventService.init();
    }

    private CacheEventService cacheEventService(EventBus eventBus, ICacheEventHandler cacheEventHandler) {
        return new CacheEventService(eventBus, cacheEventHandler);
    }

    private DefaultEventBus eventBus(ExecutorService busWorker) {
        return new DefaultEventBus(new MemoryEventStorage(), busWorker);
    }

    public static class MockEventHandler implements ICacheEventHandler {
        private AtomicInteger size = new AtomicInteger(0);


        @Override
        public boolean onEventCreate(Event<CachePayload> event) {
            try {
                Assert.assertNotNull(event);
                Assert.assertEquals(EventType.ENTITY_BUILD, event.type());

                Assert.assertTrue(event.payload().isPresent());
                resultMap.computeIfAbsent(event.payload().get().getTxId(), t -> new CopyOnWriteArrayList<>()).add(event);
                size.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        public boolean onEventUpdate(Event<CachePayload> event) {
            try {
                Assert.assertNotNull(event);
                Assert.assertEquals(ENTITY_REPLACE, event.type());

                Assert.assertTrue(event.payload().isPresent());
                resultMap.computeIfAbsent(event.payload().get().getTxId(), t -> new CopyOnWriteArrayList<>()).add(event);

                size.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean onEventDelete(Event<CachePayload> event) {
            try {
                Assert.assertNotNull(event);
                Assert.assertEquals(EventType.ENTITY_DELETE, event.type());

                Assert.assertTrue(event.payload().isPresent());
                resultMap.computeIfAbsent(event.payload().get().getTxId(), t -> new CopyOnWriteArrayList<>()).add(event);

                size.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean onEventBegin(Event<BeginPayload> event) {
            try {
                Assert.assertNotNull(event);
                Assert.assertEquals(TX_BEGIN, event.type());

                Assert.assertTrue(event.payload().isPresent());
                resultMap.computeIfAbsent(event.payload().get().getTxId(), t -> new CopyOnWriteArrayList<>()).add(event);

                size.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean onEventCommit(Event<CommitPayload> event) {
            try {
                Assert.assertNotNull(event);
                Assert.assertEquals(EventType.TX_COMMITED, event.type());

                Assert.assertTrue(event.payload().isPresent());
                resultMap.computeIfAbsent(event.payload().get().getTxId(), t -> new CopyOnWriteArrayList<>()).add(event);

                size.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {
            return null;
        }

        @Override
        public long queueSize() {
            return size.get();
        }

        @Override
        public int eventCleanByRange(long start, long end) {
            return 0;
        }

        @Override
        public void eventCleanByTxId(long txId) {

        }

        @Override
        public long expiredDuration() {
            return 3600 * 1000;
        }
    }

}
