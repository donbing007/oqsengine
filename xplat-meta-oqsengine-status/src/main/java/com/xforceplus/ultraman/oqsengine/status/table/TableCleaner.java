package com.xforceplus.ultraman.oqsengine.status.table;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  table cleaner
 */
public class TableCleaner {

    private RedisClient redisClient;

    private Long period = 10L;

    private Long initDelay = 10L;

    public TableCleaner(RedisClient redisClient, Long period, Long initDelay) {
        this.redisClient = redisClient;
        this.period = period;
        this.initDelay = initDelay;
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final static String script = "local members = redis.call('smembers', 'tables') \n" +
                            " local time = redis.call('TIME') \n" +
                            " local score_e = time[1] * 1000 + (time[2] / 1000) - (60 * 10 * 1000) \n" +
                            " for i = 1, #members do\n" +
                            "   redis.call('ZREMRANGEBYSCORE', members[i], 0, score_e) \n" +
                            " end"
            ;

    public TableCleaner(RedisClient redisClient){
        this.redisClient = redisClient;
    }

    public void run(){
        scheduledExecutorService.scheduleAtFixedRate(this::clean, initDelay, period, TimeUnit.SECONDS);
    }

    public void clean(){
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        RedisReactiveCommands<String, String> reactive = connect.reactive();
        reactive.eval(script, ScriptOutputType.STATUS).subscribe();
    }
}
