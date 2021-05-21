package com.xforceplus.ultraman.oqsengine.event.storage;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventPriority;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MemoryEventStorage Tester.
 *
 * @author dongbin
 * @version 1.0 03/24/2021
 * @since <pre>Mar 24, 2021</pre>
 */
public class MemoryEventStorageTest {

    private MemoryEventStorage eventStorage;

    @Before
    public void before() throws Exception {
        eventStorage = new MemoryEventStorage();
    }

    @After
    public void after() throws Exception {
        eventStorage = null;
    }

    @Test
    public void testPriority() throws Exception {
        // 5个低优先级
        for (int i = 0; i < 5; i++) {
            eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "low" + i, EventPriority.LOW));
        }
        // 5个普通优先级.
        for (int i = 0; i < 5; i++) {
            eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "normal" + i, EventPriority.NORMAL));
        }
        // 5个高优先级.
        for (int i = 0; i < 5; i++) {
            eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "high" + i, EventPriority.HIGH));
        }

        Assert.assertEquals(15, eventStorage.size());

        Optional<Event> eventOp;
        Event event;
        // 5个高优先级.
        for (int i = 0; i < 5; i++) {
            eventOp = eventStorage.pop();
            Assert.assertTrue(eventOp.isPresent());

            event = eventOp.get();
            Assert.assertEquals(EventPriority.HIGH, event.priority());
        }
        // 5个普通优先级
        for (int i = 0; i < 5; i++) {
            eventOp = eventStorage.pop();
            Assert.assertTrue(eventOp.isPresent());

            event = eventOp.get();
            Assert.assertEquals(EventPriority.NORMAL, event.priority());
        }
        // 5个低优先级
        for (int i = 0; i < 5; i++) {
            eventOp = eventStorage.pop();
            Assert.assertTrue(eventOp.isPresent());

            event = eventOp.get();
            Assert.assertEquals(EventPriority.LOW, event.priority());
        }
    }

    @Test
    public void testSize() throws Exception {
        Assert.assertEquals(0, eventStorage.size());
        CountDownLatch latch = new CountDownLatch(15);
        CompletableFuture.runAsync(() -> {
            // 5个低优先级
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "low" + i, EventPriority.LOW));
                latch.countDown();
            }
        });
        CompletableFuture.runAsync(() -> {
            // 5个普通优先级.
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "normal" + i, EventPriority.NORMAL));
                latch.countDown();
            }
        });
        CompletableFuture.runAsync(() -> {
            // 5个高优先级.
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "high" + i, EventPriority.HIGH));
                latch.countDown();
            }
        });

        latch.await(6, TimeUnit.SECONDS);

        Assert.assertEquals(15, eventStorage.size());
    }

    @Test
    public void testClear() throws Exception {
        CountDownLatch latch = new CountDownLatch(15);
        CompletableFuture.runAsync(() -> {
            // 5个低优先级
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "low" + i, EventPriority.LOW));
                latch.countDown();
            }
        });
        CompletableFuture.runAsync(() -> {
            // 5个普通优先级.
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "normal" + i, EventPriority.NORMAL));
                latch.countDown();
            }
        });
        CompletableFuture.runAsync(() -> {
            // 5个高优先级.
            for (int i = 0; i < 5; i++) {
                eventStorage.push(new TestEvent(EventType.ENTITY_BUILD, "high" + i, EventPriority.HIGH));
                latch.countDown();
            }
        });

        latch.await(6, TimeUnit.SECONDS);

        eventStorage.clear();

        Assert.assertEquals(0, eventStorage.size());
        Assert.assertFalse(eventStorage.pop().isPresent());
    }

    static class TestEvent extends ActualEvent<String> {

        private EventPriority priority;

        public TestEvent(EventType type, String payload, EventPriority priority) {
            super(type, payload);
            this.priority = priority;
        }

        @Override
        public EventPriority priority() {
            return this.priority;
        }
    }
} 
