package com.xforceplus.ultraman.oqsengine.event;

import com.xforceplus.ultraman.oqsengine.event.storage.EventStorage;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DefaultEventBus Tester.
 *
 * @author dongbin
 * @version 1.0 03/24/2021
 * @since <pre>Mar 24, 2021</pre>
 */
public class DefaultEventBusTest {

    private DefaultEventBus eventBus;
    private EventStorage eventStorage;
    private ExecutorService worker;

    @Before
    public void before() throws Exception {
        eventStorage = new MemoryEventStorage();
        worker = Executors.newFixedThreadPool(3);
        eventBus = new DefaultEventBus(eventStorage, worker);
        eventBus.init();
    }

    @After
    public void after() throws Exception {
        eventBus.destroy();
        worker.shutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoEventStorage() throws Exception {
        new DefaultEventBus(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoWorker() throws Exception {
        new DefaultEventBus(eventStorage, null);
    }

    /**
     * 测试是否正确通知.
     */
    @Test
    public void testNotice() throws Exception {
        AtomicInteger size = new AtomicInteger(200);
        CountDownLatch latch = new CountDownLatch(size.get());

        eventBus.watch(EventType.ENTITY_BUILD, (event) -> {

            Assert.assertEquals(event.type(), EventType.ENTITY_BUILD);
            Assert.assertTrue(event.payload().isPresent());
            Assert.assertTrue(event.payload().get().toString().startsWith("test"));

            size.decrementAndGet();
            latch.countDown();
        });

        eventBus.watch(EventType.ENTITY_BUILD, (event) -> {

            Assert.assertEquals(event.type(), EventType.ENTITY_BUILD);
            Assert.assertTrue(event.payload().isPresent());
            Assert.assertTrue(event.payload().get().toString().startsWith("test"));

            size.decrementAndGet();
            latch.countDown();
        });


        for (int i = 0; i < 100; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                eventBus.notify(new TestEvent(EventType.ENTITY_BUILD, "test" + finalI));
            });
        }


        latch.await(6, TimeUnit.SECONDS);

        Assert.assertEquals(0, size.get());
    }

    /**
     * 没有关注的事件将被忽略.
     */
    @Test
    public void testNoNotice() throws Exception {
        eventBus.notify(new TestEvent(EventType.ENTITY_DELETE, "delete0"));
        Assert.assertEquals(0, eventStorage.size());
    }

    static class TestEvent extends ActualEvent<String> {

        public TestEvent(EventType type, String payload) {
            super(type, payload);
        }
    }
} 
