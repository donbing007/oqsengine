package com.xforceplus.ultraman.oqsengine.event;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.event.storage.EventStorage;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 默认的事件总线实现.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 11:13
 * @since 1.8
 */
public class DefaultEventBus implements EventBus, Lifecycle {

    private ConcurrentMap<EventType, Queue<Consumer<Event>>> listeners;
    private EventStorage eventStorage;
    private ExecutorService worker;
    private volatile boolean closed;

    /**
     * 构造默认事务总线实例.
     *
     * @param eventStorage 事件储存器.
     * @param worker       工作者线程组.
     */
    public DefaultEventBus(EventStorage eventStorage, ExecutorService worker) {
        this.listeners = new ConcurrentHashMap<>();
        this.eventStorage = eventStorage;
        this.worker = worker;

        if (this.eventStorage == null) {
            throw new IllegalArgumentException("Invalid EventStorage instance.");
        }

        if (worker == null) {
            throw new IllegalArgumentException("Invalid ExecutorService instance.");
        }
    }

    @PostConstruct
    @Override
    public void init() {
        if (!closed) {
            closed = false;

            worker.submit(new Distributor());
        }
    }

    @PreDestroy
    @Override
    public void destroy() {
        if (!closed) {
            closed = true;

            listeners.clear();
        }
    }

    @Override
    public void watch(EventType type, Consumer<Event> listener) {
        Queue<Consumer<Event>> existTypeListeners = listeners.computeIfAbsent(type, t -> new ConcurrentLinkedQueue());

        existTypeListeners.offer(listener);

    }

    @Override
    public void notify(Event event) {
        EventType type = event.type();
        Queue<Consumer<Event>> eventlisteners = listeners.get(type);
        if (eventlisteners == null || eventlisteners.isEmpty()) {
            // 事件无关注.
            return;
        } else {
            if (!eventStorage.push(event)) {
                throw new IllegalStateException("Can not notify event!");
            }
        }
    }

    /**
     * 事件分配者.
     */
    private class Distributor implements Runnable {

        @Override
        public void run() {
            while (!closed) {
                Optional<Event> eventOp = eventStorage.pop();

                if (eventOp.isPresent()) {

                    Event event = eventOp.get();
                    Queue<Consumer<Event>> eventListeners = listeners.get(event.type());
                    if (eventListeners != null && !eventListeners.isEmpty()) {
                        worker.submit(new Noticer(event, eventListeners));
                    }

                } else {
                    // 进行短暂的休眠.
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
                }
            }
        }
    }

    /**
     * 事件通知者.
     */
    private static class Noticer implements Runnable {

        private Event event;
        private Queue<Consumer<Event>> listeners;

        public Noticer(Event event, Queue<Consumer<Event>> listeners) {
            this.event = event;
            this.listeners = listeners;
        }

        @Override
        public void run() {
            for (Consumer<Event> listener : listeners) {
                listener.accept(event);
            }
        }
    }

}
