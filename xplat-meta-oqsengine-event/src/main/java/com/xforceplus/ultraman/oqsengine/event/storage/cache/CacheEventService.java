package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.cache.CachePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * desc :
 * name : CacheEventService
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public class CacheEventService implements ICacheEventService {

    final static Logger logger = LoggerFactory.getLogger(CacheEventService.class);

    private EventBus eventBus;

    private ICacheEventHandler cacheEventHandler;

    private Thread thread;

    private static volatile boolean closed;

    private static final long CLOSE_WAIT_DURATION = 1000;
    private static final long CLOSE_WAIT_MAX_LOOP = 60;

    public CacheEventService(EventBus eventBus, ICacheEventHandler cacheEventHandler) {
        this.eventBus = eventBus;
        this.cacheEventHandler = cacheEventHandler;
    }


    @PostConstruct
    public void init(){
        closed = false;

        if (null == thread) {
            thread = new Thread(new Cleaner(cacheEventHandler, cacheEventHandler.expiredDuration()));
            thread.start();
        }

        eventBus.watch(EventType.ENTITY_BUILD, x -> {
            execute(EventType.ENTITY_BUILD, this::build, x);
        });

        eventBus.watch(EventType.ENTITY_REPLACE, x -> {
            execute(EventType.ENTITY_REPLACE, this::replace, x);
        });

        eventBus.watch(EventType.ENTITY_DELETE, x -> {
            execute(EventType.ENTITY_REPLACE, this::delete, x);
        });

        eventBus.watch(EventType.TX_BEGIN, x -> {
            execute(EventType.TX_BEGIN, cacheEventHandler::onEventBegin, x);
        });

        eventBus.watch(EventType.TX_COMMITED, x -> {
            execute(EventType.TX_COMMITED, cacheEventHandler::onEventCommit, x);
        });
    }

    @PreDestroy
    public void destroy() {
        closed = true;
        if (null != thread) {
            waitForClosed(thread, CLOSE_WAIT_MAX_LOOP);
        }
    }

    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {
        return cacheEventHandler.eventsQuery(txId, id, version, eventType);
    }

    @Override
    public void cleanByTxId(long txId) {
        cacheEventHandler.eventCleanByTxId(txId);
    }

    @Override
    public void cleanByTimeRange(long start, long end) {
        cacheEventHandler.eventCleanByRange(start, end);
    }

    @Override
    public long size() {
        return cacheEventHandler.queueSize();
    }

    private static class Cleaner implements Runnable {

        final Logger logger = LoggerFactory.getLogger(Cleaner.class);

        private ICacheEventHandler cacheEventHandler;
        private long cleanDuration;

        public Cleaner(ICacheEventHandler cacheEventHandler, long cleanDuration) {
            this.cacheEventHandler = cacheEventHandler;
            this.cleanDuration = cleanDuration;
        }

        @Override
        public void run() {
            while (!closed) {
                int count = 0;
                try {
                    count = cacheEventHandler.eventCleanByRange(0, System.currentTimeMillis() - cleanDuration);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }

                if (0 == count) {
                    // 休眠10秒后继续
                    sleep(CLOSE_WAIT_DURATION);
                }
            }
        }
    }

    private void build(Event<BuildPayload> e) {
        BuildPayload buildPayload = e.payload().get();

        ActualEvent<CachePayload> cachePayload =
                new ActualEvent<>(e.type(),
                        new CachePayload(buildPayload.getTxId(), buildPayload.getNumber(),
                                buildPayload.getEntity().id(), buildPayload.getEntity().version(),
                                entityValues(buildPayload.getEntity())),
                        e.time()
                );
        cacheEventHandler.onEventCreate(cachePayload);
    }

    private void replace(Event<ReplacePayload> e) {
        ReplacePayload replacePayload = e.payload().get();

        ActualEvent<CachePayload> cachePayload =
                new ActualEvent<>(e.type(),
                        new CachePayload(replacePayload.getTxId(), replacePayload.getNumber(),
                                replacePayload.getEntity().id(), replacePayload.getEntity().version(),
                                entityValues(replacePayload.getEntity())),
                        e.time()
                );
        cacheEventHandler.onEventUpdate(cachePayload);
    }

    private void delete(Event<DeletePayload> e) {
        DeletePayload deletePayload = e.payload().get();

        ActualEvent<CachePayload> cachePayload =
                new ActualEvent<>(e.type(),
                        new CachePayload(deletePayload.getTxId(), deletePayload.getNumber(),
                                deletePayload.getEntity().id(), deletePayload.getEntity().version(),
                                entityValues(deletePayload.getEntity())),
                        e.time()
                );
        cacheEventHandler.onEventDelete(cachePayload);
    }

    private void execute(EventType eventType, Consumer<Event> consumer, Event e) {
        if (null != e && (e.payload().isPresent())) {
            consumer.accept(e);
        } else {
            logger.warn("{} triggered, but event is null or payload is null..", eventType.name());
        }
    }

    private Map<IEntityField, Object> entityValues(IEntity entity) {
        Collection<IValue> values = entity.entityValue().values();
        return values.stream().collect(Collectors.toMap(f1 -> (EntityField) f1.getField(), f1 -> ((IValue) f1.getValue()).getValue(), (f1, f2) -> f1));
    }

    private static void waitForClosed(Thread thread, long maxWaitLoops) {
        for (int i = 0; i < maxWaitLoops; i++) {
            if (!thread.isAlive()) {
                logger.info("wait for loops [{}] and thread closed successful.", i);
                break;
            }
            sleep(CLOSE_WAIT_DURATION);
        }
        logger.info("reach max wait loops [{}] and thread will be force-closed.", maxWaitLoops);
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
