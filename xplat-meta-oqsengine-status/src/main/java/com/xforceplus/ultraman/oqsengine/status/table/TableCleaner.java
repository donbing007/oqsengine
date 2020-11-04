package com.xforceplus.ultraman.oqsengine.status.table;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;

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

    private Long bufferTime = 10L;

    private String script;

    private StatefulRedisConnection<String, String> connect;

    private final static String scriptTemplate = "local members = redis.call('smembers', 'tables') \n" +
            " local time = redis.call('TIME') \n" +
            " local score_e = time[1] * 1000 + (time[2] / 1000) - %s \n" +
            " for i = 1, #members do\n" +
            "   redis.call('ZREMRANGEBYSCORE', members[i], 0, score_e) \n" +
            " end";

    public TableCleaner(RedisClient redisClient, Long period, Long initDelay, Long bufferTime) {
        this.redisClient = redisClient;
        this.period = period;
        this.initDelay = initDelay;
        this.bufferTime = bufferTime;
        this.script = String.format(scriptTemplate, bufferTime);
        this.connect = redisClient.connect();
        RedisScriptingCommands<String, String> sync = connect.sync();
        sync.scriptLoad(script);
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);



    public TableCleaner(RedisClient redisClient){
        this.redisClient = redisClient;
    }

    public void run(){
        scheduledExecutorService.scheduleAtFixedRate(this::clean, initDelay, period, TimeUnit.SECONDS);
    }

    public void clean(){
        RedisScriptingReactiveCommands<String, String> reactive = connect.reactive();
        reactive.eval(script, ScriptOutputType.STATUS).subscribe();
    }
}
