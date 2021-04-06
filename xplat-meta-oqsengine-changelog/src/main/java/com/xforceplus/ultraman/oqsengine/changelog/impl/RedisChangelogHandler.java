package com.xforceplus.ultraman.oqsengine.changelog.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.ChangelogHandler;
import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Redis implemention
 *
 * @param <T>
 */
public class RedisChangelogHandler<T> implements ChangelogHandler<T> {

    private RedisClient redisClient;

    private Gateway<ChangelogCommand, ChangelogEvent> gateway;

    private ObjectMapper mapper;

    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    private Logger logger = LoggerFactory.getLogger(ChangelogHandler.class);

    private StatefulRedisConnection statefulRedisConnection;
    private RedisCommands<String, String> syncCommands;

    private Thread daemonThread;

    private long expireTime = 10;

    private AtomicBoolean isLeader = new AtomicBoolean(false);

    private ScheduledExecutorService consumer;

    private ScheduledExecutorService mover;

    private long initDelay = 10;

    private long period = 10;

    private long blockTime = 3000;

    private long count = 100;

    /**
     * move delay
     */
    private long move_time_threshold = 10000;

    /**
     * move count delay
     */
    private long move_size_threshold = 10;

    /**
     * redis queue name
     */
    private String queueName;

    private String queueCurrentName;

    private String queueStreamName;

    private String leaderKey = "current";

    private String nodeName;

    private String SEND_SCRIPT =
            "redis.call('ZADD', '%s', KEYS[1], KEYS[2]);" +
                    "redis.call('SET', '%s', KEYS[1]..':'..KEYS[3]);" +
                    "return true;";

    private String SPLIT_FUNCTION =
            "local split = function (s, delimiter)\n" +
                    "    local result = {};\n" +
                    "    for match in (s..delimiter):gmatch(\"(.-)\"..delimiter) do\n" +
                    "        table.insert(result, match);\n" +
                    "    end\n" +
                    "    return result;\n" +
                    "end; \n";


    /**
     * TODO calculate the number
     * ZCOUNT myzset -inf +inf
     * <p>
     * KEY
     * 1: currentTaskKey
     * 2: currentTimestamp
     * 3: time threshold
     * 4: commit threshold
     * 5: set name
     * 6: stream name
     */
    private String MOVE_AND_ADD =
            SPLIT_FUNCTION +
                    "redis.log(2, 'start');" +
                    "local currentTask = redis.call('GET', KEYS[1]);" +
                    "if currentTask ~= false then \n" +
                    "  redis.log(2, 'got task');" +
                    "  redis.log(2, currentTask);" +
                    "  local splitValue = split(currentTask, ':');" +
                    "  redis.log(2, 'split value');" +
                    "  redis.log(2, splitValue[1]);" +
                    "  redis.log(2, splitValue[2]);" +
                    "  local taskCommitId = tonumber(splitValue[1]);" +
                    "  local taskTimestamp = tonumber(splitValue[2]);" +
                    "  local size;" +
                    "  redis.log(2, KEYS[1]);" +
                    "  redis.log(2, KEYS[2]);" +
                    "  redis.log(2, 'Queue Name');" +
                    "  redis.log(2, KEYS[5]);" +
                    "  if (tonumber(KEYS[2]) - taskTimestamp) > tonumber(KEYS[3])\n" +
                    "  then \n" +
                    "    redis.log(2, 'in here');" +
                    "    size = redis.call('ZCOUNT', KEYS[5], '0', '+inf');\n" +
                    "    redis.log(2, 'here end');" +
                    "  else \n" +
                    "    redis.log(2, 'in there');" +
                    "    local lastFetchCommit = taskCommitId - tonumber(KEYS[4]);" +
                    "    redis.log(2, lastFetchCommit);" +
                    "    size = redis.call('ZCOUNT', KEYS[5], '0', lastFetchCommit)" +
                    "  end \n" +
                    "  redis.log(2, 'GOT SIZE');" +
                    "  redis.log(2, size);" +
                    "  local result = redis.call('ZPOPMIN', KEYS[5], size);" +
                    "  for i = 1, #result do \n" +
                    "    if i % 2 ~= 0 then" +
                    "      redis.call('XADD', KEYS[6], '*', 'payload', result[i]);" +
                    "    end\n " +
                    "  end\n" +
                    "end";

    /**
     * scripts shas
     */
    private String sendScriptSha;
    private String moveScriptSha;

    public RedisChangelogHandler(String nodeName, String queueName, RedisClient redisClient, Gateway gateway, ObjectMapper mapper) {
        this.queueName = queueName;
        this.queueCurrentName = queueName.concat("_current");
        this.queueStreamName = queueName.concat("_stream");
        this.nodeName = nodeName;
        this.syncCommands = redisClient.connect().sync();
        this.sendScriptSha = syncCommands
                .scriptLoad(String
                        .format(SEND_SCRIPT
                                , queueName
                                , queueName.concat("_current")));

        this.moveScriptSha = syncCommands.scriptLoad(MOVE_AND_ADD);

        this.redisClient = redisClient;
        this.gateway = gateway;
        this.mapper = mapper;
    }

    /**
     * TODO
     *
     * @param source
     * @return
     */
    @Override
    public TransactionalChangelogEvent getEvent(T source) {
        return null;
    }

    @Override
    public void handle(TransactionalChangelogEvent transaction) {

        Long commitId = transaction.getCommitId();
        String payload = "";

        try {
            payload = mapper.writeValueAsString(transaction);

            String[] keys = {
                    commitId.toString(),
                    payload,
                    Long.toString(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli())
            };

            syncCommands.evalsha(sendScriptSha, ScriptOutputType.BOOLEAN, keys);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * start consumer
     */
    @Override
    public void prepareConsumer() {
        daemonThread = new Thread(() -> {
            while(true) {
                Object current = syncCommands.get(leaderKey);
                if (current == null || current.equals(nodeName)) {
                    syncCommands.setex(leaderKey, expireTime, nodeName);
                    isLeader.compareAndSet(false, true);
                } else {
                    isLeader.compareAndSet(true, false);
                }

                try {
                    Thread.sleep(expireTime / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        daemonThread.setDaemon(true);
        daemonThread.start();

        mover = new ScheduledThreadPoolExecutor(1, ExecutorHelper.buildNameThreadFactory("Changelog-mover"));
        startMover();

        consumer = new ScheduledThreadPoolExecutor(1, ExecutorHelper.buildNameThreadFactory("Changelog-consumer"));
        startConsumer();

        initGroup();
    }

    private void startConsumer() {
        consumer.scheduleAtFixedRate(() -> {
            if (isLeader.get()) {
                logger.info("{}:Leader is me", nodeName);
                doConsume();
            }
        }, initDelay, period, TimeUnit.SECONDS);
    }

    private void startMover() {
        mover.scheduleAtFixedRate(() -> {
            if (isLeader.get()) {
                logger.info("{}:Leader is me", nodeName);
                doMove();
            }
        }, initDelay, period, TimeUnit.SECONDS);
    }


    /**
     * TODO calculate the number
     * ZCOUNT myzset -inf +inf
     * <p>
     * <p>
     * KEY
     * 1: currentTaskKey
     * 2: currentTimestamp
     * 3: time threshold
     * 4: commit threshold
     * 5: set name
     * 6: stream name
     */
    private void doMove() {
        String[] keys = new String[]{
                queueCurrentName,
                Long.toString(LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()),
                Long.toString(move_time_threshold),
                Long.toString(move_size_threshold),
                queueName,
                queueStreamName
        };
        syncCommands.evalsha(moveScriptSha, ScriptOutputType.BOOLEAN, keys);
    }

    private void doConsume() {
        consumeAndDeliver();
    }

    private void initGroup() {
        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.latest(queueStreamName)
                    , "group", XGroupCreateArgs.Builder.mkstream());
        } catch (Exception ex) {
            System.out.println("Group is created");
        }
    }

    private List<ChangelogCommand> toChangeCommandList(StreamMessage<String, String> x) {
        Map<String, String> body = x.getBody();
        String payload = body.get("payload");

        try {
            TransactionalChangelogEvent event = mapper.readValue(payload, TransactionalChangelogEvent.class);
            return event.getChangedEventList().stream()
                    .map(evt -> {
                        return new AddChangelog(evt.getId(), evt.getEntityClassId(), evt);
                    }).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            logger.error("{}", e);
            return Collections.emptyList();
        }
    }

    private void consumeAndDeliver() {
        List<StreamMessage<String, String>> xreadgroup = syncCommands.xreadgroup(Consumer.from("group", nodeName)
                , XReadArgs.Builder
                        .block(blockTime)
                        .count(count), XReadArgs.StreamOffset.lastConsumed(queueStreamName));

        if (xreadgroup == null || xreadgroup.isEmpty()) {
            return;
        }

        logger.info("{}:Got Message", nodeName);

        /**
         * dispatch the command
         */
        xreadgroup.forEach(x -> {
            toChangeCommandList(x).forEach(command -> {
                logger.info("got changeCommand and deliver to the gateway");
                try {
                    gateway.fireAndForget(command, new HashMap<>());
                } catch (Exception ex){
                    logger.error("{}", ex);
                }

                syncCommands.xack(queueStreamName, "group", x.getId());
            });
        });
    }
}
