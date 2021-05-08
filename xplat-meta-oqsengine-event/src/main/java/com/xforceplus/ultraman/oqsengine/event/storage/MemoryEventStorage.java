package com.xforceplus.ultraman.oqsengine.event.storage;

import com.xforceplus.ultraman.oqsengine.event.Event;
import java.util.Comparator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于内存非持久化的事件储存.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 11:37
 * @since 1.8
 */
public class MemoryEventStorage implements EventStorage {

    private Queue<Event> eventQueue = new PriorityBlockingQueue(16, new EventComparator().reversed());
    private AtomicInteger size = new AtomicInteger(0);

    @Override
    public boolean push(Event event) {
        if (eventQueue.offer(event)) {
            size.incrementAndGet();
            return true;
        }

        return false;
    }

    @Override
    public Optional<Event> pop() {
        Event event = eventQueue.poll();
        if (event != null) {
            size.decrementAndGet();
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void clear() {
        size.set(0);
        eventQueue.clear();
    }

    @Override
    public int size() {
        return size.get();
    }

    static class EventComparator implements Comparator<Event> {

        @Override
        public int compare(Event o1, Event o2) {
            if (o1.priority().getValue() < o2.priority().getValue()) {
                return -1;
            } else if (o1.priority().getValue() > o2.priority().getValue()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
