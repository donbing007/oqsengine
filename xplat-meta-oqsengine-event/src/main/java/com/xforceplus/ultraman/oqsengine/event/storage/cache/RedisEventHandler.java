package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * desc :
 * name : RedisEventHandler
 *
 * @author : xujia
 * date : 2021/4/8
 * @since : 1.8
 */
public class RedisEventHandler implements ICacheEventHandler {

    final Logger logger = LoggerFactory.getLogger(RedisEventHandler.class);

    public String CUD_PAYLOAD_HASH_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.event.payload";

    public String TX_EXPIRE_HASH_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.hash";

    public String TX_EXPIRE_ZSORT_KEY = "com.xforceplus.ultraman.oqsengine.event.tx.zsort";

    public String STREAM_TX_ID = "com.xforceplus.ultraman.oqsengine.event.stream.tx";

    public int MAX_WAIT_LOOP = 100;

    private ExecutorService worker;

    private RedisClient redisClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private StatefulRedisConnection<String, String> syncConnect;

    private RedisCommands<String, String> syncCommands;

    public RedisEventHandler(RedisClient redisClient, ExecutorService worker) {
        this.redisClient = redisClient;
        this.worker = worker;

        if (this.redisClient == null) {
            throw new IllegalArgumentException("Invalid RedisClient instance.");
        }

        if (worker == null) {
            throw new IllegalArgumentException("Invalid ExecutorService instance.");
        }
    }

    @PostConstruct
    public void init() {
        if (redisClient == null) {
            throw new IllegalStateException("Invalid redisClient.");
        }

        syncConnect = redisClient.connect();
        syncCommands = syncConnect.sync();
        syncCommands.clientSetname("oqs.event");
    }

    @Override
    public boolean onEventCreate(Event<BuildPayload> event) {
        if (null != event && event.payload().isPresent()) {
            if (!eventChange(event.payload().get().getTxId(), event.payload().get().getEntity().id()
                    , event.payload().get().getEntity().version(), event)) {
                worker.submit(new ReCover(this::onEventCreate, event));
            }
        }

        return true;
    }

    @Override
    public boolean onEventUpdate(Event<ReplacePayload> event) {
        if (null != event && event.payload().isPresent()) {
            if(!eventChange(event.payload().get().getTxId(), event.payload().get().getEntity().id()
                    , event.payload().get().getEntity().version(), event)) {
                worker.submit(new ReCover(this::onEventUpdate, event));
            }
        }

        return true;
    }

    @Override
    public boolean onEventDelete(Event<DeletePayload> event) {
        if (null != event && event.payload().isPresent()) {
            if(!eventChange(event.payload().get().getTxId(), event.payload().get().getEntity().id()
                    , event.payload().get().getEntity().version(), event)) {
                worker.submit(new ReCover(this::onEventDelete, event));
            }
        }

        return true;
    }

    @Override
    public boolean onEventBegin(Event<BeginPayload> event) {
        if (null != event && event.payload().isPresent()) {
            try {
                long timeStamp = event.time();

                String txIdStr = Long.toString(event.payload().get().getTxId());

                if (0 == syncCommands.zadd(TX_EXPIRE_ZSORT_KEY, timeStamp, txIdStr)) {
                    throw new RuntimeException(String.format("zAdd [%s] error, add to retry...", txIdStr));
                }

                if (!syncCommands.hset(TX_EXPIRE_HASH_KEY, txIdStr, Long.toString(timeStamp))) {
                    throw new RuntimeException(String.format("hSet [%s] error, add to retry...", txIdStr));
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());

                worker.submit(new ReCover(this::onEventBegin, event));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onEventCommit(Event<CommitPayload> event) {
        if (null != event && event.payload().isPresent()) {
            if (event.payload().get().getMaxOpNumber() > 0) {
                try {
                    for (int i = 0; i < MAX_WAIT_LOOP; i++) {
                        Long len = syncCommands.hlen(eventKeyGenerate(event.payload().get().getTxId()));
                        if (null != len && len == event.payload().get().getMaxOpNumber()) {
                            //  将txId加入到stream中
                            syncCommands.xadd(STREAM_TX_ID, Long.toString(event.payload().get().getTxId()), event.time());
                            return true;
                        }
                        //  短暂等待1毫秒后重试
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
                    }
                    throw new RuntimeException(String.format("rPushX [%d] error, reach max wait, add to retry...", event.payload().get().getTxId()));
                } catch (Exception e) {
                    logger.warn(e.getMessage());

                    worker.submit(new ReCover(this::onEventCommit, event));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int eventCleanByRange(long start, long end) {
        Range<Long> r = Range.create(start, end);
        List<String> needCleans = syncCommands.zrangebyscore(TX_EXPIRE_ZSORT_KEY, r);
        if (null != needCleans && needCleans.size() > 0) {
            for (String need : needCleans) {
                clean(need);
            }
            syncCommands.zremrangebyscore(TX_EXPIRE_ZSORT_KEY, r);
            return needCleans.size();
        }
        return 0;
    }

    @Override
    public void eventCleanByTxId(long txId) {
        String txIdStr = Long.toString(txId);

        clean(txIdStr);

        syncCommands.zrem(TX_EXPIRE_ZSORT_KEY, txIdStr);
    }

    @Override
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType) {

        if (null == id || null == version || invalidQueryEventType(eventType)) {
            //  查询对应txId的列表
            Map<String, String> result = syncCommands.hgetall(eventKeyGenerate(txId));
            return null == result || result.isEmpty() ? Collections.emptyList() : result.values();
        }

        //  精确查询 txId + id + version 确定
        String rBuild = syncCommands.hget(eventKeyGenerate(txId), eventFieldGenerate(id, version, eventType));
        return null == rBuild || rBuild.isEmpty() ? Collections.emptyList() : Collections.singletonList(rBuild);
    }

    @Override
    public long queueSize() {
        Long size = syncCommands.xlen(STREAM_TX_ID);
        return null == size ? 0 : size;
    }

    private void clean(String txIdString) {

        //  清理txId
        syncCommands.hdel(TX_EXPIRE_HASH_KEY, txIdString);

        //  清理event
        syncCommands.del(eventKeyGenerate(txIdString));
    }

    private boolean eventChange(long txId, long id, long version, Event<?> event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            return syncCommands.hset(eventKeyGenerate(txId), eventFieldGenerate(id, version, event.type().getValue()), json);
        } catch (Exception e) {
            logger.warn("eventChange error, [{}-{}-{}-{}], add to retry... ", txId, event.type(), id, version);

            return false;
        }
    }

    private String eventFieldGenerate(long id, long version, int eventType) {
        return  String.format("%d.%d.%d", id, version, eventType);
    }

    private String eventKeyGenerate(long txId) {
        return eventKeyGenerate(Long.toString(txId));
    }

    private String eventKeyGenerate(String txIdString) {
        return String.format("%s.%s", CUD_PAYLOAD_HASH_KEY_PREFIX, txIdString);
    }

    private boolean invalidQueryEventType(Integer eventType) {
        return null == eventType ||
                (eventType != EventType.ENTITY_BUILD.getValue() &&
                        eventType != EventType.ENTITY_REPLACE.getValue() &&
                        eventType != EventType.ENTITY_DELETE.getValue());
    }

    private static class ReCover implements Runnable {

        private Consumer<Event> consumer;

        private Event event;

        public ReCover(Consumer<Event> consumer, Event event) {
            this.consumer = consumer;
            this.event = event;
        }

        @Override
        public void run() {
            consumer.accept(event);
        }
    }
}
