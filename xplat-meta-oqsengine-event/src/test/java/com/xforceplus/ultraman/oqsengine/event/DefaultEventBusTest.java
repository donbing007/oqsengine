package com.xforceplus.ultraman.oqsengine.event;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xforceplus.ultraman.oqsengine.event.storage.EventStorage;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void before() throws Exception {
        eventStorage = new MemoryEventStorage();
        worker = Executors.newFixedThreadPool(3);
        eventBus = new DefaultEventBus(eventStorage, worker);
        eventBus.init();
    }

    @AfterEach
    public void after() throws Exception {
        eventBus.destroy();
        worker.shutdown();
    }

    @Test
    public void testConstructorNoEventStorage() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultEventBus(null, null);
        });
    }

    @Test
    public void testConstructorNoWorker() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultEventBus(eventStorage, null);
        });
    }

    /**
     * 测试是否正确通知.
     */
    @Test
    public void testNotice() throws Exception {
        AtomicInteger size = new AtomicInteger(200);
        CountDownLatch latch = new CountDownLatch(size.get());

        eventBus.watch(EventType.ENTITY_BUILD, (event) -> {

            Assertions.assertEquals(event.type(), EventType.ENTITY_BUILD);
            Assertions.assertTrue(event.payload().isPresent());
            Assertions.assertTrue(event.payload().get().toString().startsWith("test"));

            size.decrementAndGet();
            latch.countDown();
        });

        eventBus.watch(EventType.ENTITY_BUILD, (event) -> {

            Assertions.assertEquals(event.type(), EventType.ENTITY_BUILD);
            Assertions.assertTrue(event.payload().isPresent());
            Assertions.assertTrue(event.payload().get().toString().startsWith("test"));

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

        Assertions.assertEquals(0, size.get());
    }

    /**
     * 没有关注的事件将被忽略.
     */
    @Test
    public void testNoNotice() throws Exception {
        eventBus.notify(new TestEvent(EventType.ENTITY_DELETE, "delete0"));
        Assertions.assertEquals(0, eventStorage.size());
    }

    static class TestEvent extends ActualEvent<String> {

        public TestEvent(EventType type, String payload) {
            super(type, payload);
        }
    }
} 
