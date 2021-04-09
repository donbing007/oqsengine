package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;

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

    //  3小时过期
    private static final long CLEAN_BUFFER_TIME = 3 * 60 * 60 * 1000;
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
            thread = new Thread(new Cleaner(cacheEventHandler));
            thread.start();
        }

        eventBus.watch(EventType.ENTITY_BUILD, x -> {
            cacheEventHandler.onEventCreate((ActualEvent<BuildPayload>) x);
        });

        eventBus.watch(EventType.ENTITY_DELETE, x -> {
            cacheEventHandler.onEventDelete((ActualEvent<DeletePayload>) x);
        });

        eventBus.watch(EventType.ENTITY_REPLACE, x -> {
            cacheEventHandler.onEventUpdate((ActualEvent<ReplacePayload>) x);
        });


        eventBus.watch(EventType.TX_BEGIN, x -> {
            cacheEventHandler.onEventBegin((ActualEvent<BeginPayload>) x);
        });

        eventBus.watch(EventType.TX_COMMITED, x -> {
            cacheEventHandler.onEventCommit((ActualEvent<CommitPayload>) x);
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

        public Cleaner(ICacheEventHandler cacheEventHandler) {
            this.cacheEventHandler = cacheEventHandler;
        }

        @Override
        public void run() {
            while (!closed) {
                int count = 0;
                try {
                    count =
                            cacheEventHandler.eventCleanByRange(0, System.currentTimeMillis() - CLEAN_BUFFER_TIME);
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
