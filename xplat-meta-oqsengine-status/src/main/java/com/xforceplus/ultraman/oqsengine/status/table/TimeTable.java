package com.xforceplus.ultraman.oqsengine.status.table;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import io.lettuce.core.api.reactive.RedisSortedSetReactiveCommands;
import io.lettuce.core.api.sync.RedisSetCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * TimeTable
 */
public class TimeTable {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    private String tableName;

    private final static String ZDD_WITH_TIME_AND_ID =
            "local t = redis.call('TIME') \n" +
                    "return redis.call('ZADD', '%s', 'ch',  t[1] * 1000 + (t[2] / 1000 ), '%s')";

    private final static String ZRANGE_WITH_TIME =
            "local t = redis.call('TIME') \n" +
                    "return redis.call('ZRANGEBYSCORE', '%s', (t[1] * 1000 + (t[2] / 1000)) - %s , (t[1] * 1000 + (t[2] / 1000)) + %s)";


    private Logger logger = LoggerFactory.getLogger(TimeTable.class);

    public TimeTable(RedisClient redisClient, String tableName) {
        this.redisClient = redisClient;
        this.tableName = tableName;
        connection = redisClient.connect();

        //register table in redis
        //TODO
        RedisSetCommands<String, String> sync = connection.sync();
        Long added = sync.sadd("tables", tableName);
        logger.info("Register table ret {}", added);
    }

    /**
     * insert with specified time
     *
     * @param transId
     * @param timeInMillion
     * @return
     */
    public Mono<Long> insertWithLocalTime(String transId, Long timeInMillion) {
        RedisSortedSetReactiveCommands<String, String> reactive = connection.reactive();
        return reactive.zadd(tableName, ZAddArgs.Builder.ch(), timeInMillion, transId);
    }


    /**
     * insert with redis time
     * TODO if fails TEST
     *
     * @param transId
     * @return
     */
    public Mono<Long> insertWithRemoteTime(String transId) {
        RedisScriptingReactiveCommands<String, String> reactive = connection.reactive();
        String script = String.format(ZDD_WITH_TIME_AND_ID, tableName, transId);
        return reactive.eval(script, ScriptOutputType.INTEGER).single().map(x -> Long.parseLong(x.toString()));
    }

    /**
     * query from range local time
     *
     * @param startInMilli
     * @param endInMilli
     * @return
     */
    public Flux<Long> queryByLocalTime(Long startInMilli, Long endInMilli) {
        RedisSortedSetReactiveCommands<String, String> reactive = connection.reactive();
        return reactive.zrangebyscore(tableName, Range.create(startInMilli  , endInMilli))
                .map(Long::parseLong);
    }

    public Flux<Long> queryByWindow(Long lessInMilli, Long moreInMilli) {
        RedisScriptingReactiveCommands<String, String> reactive = connection.reactive();
        String script = String.format(ZRANGE_WITH_TIME, tableName, lessInMilli, moreInMilli );
        return reactive.eval(script, ScriptOutputType.MULTI).flatMap(x -> Flux.fromIterable((List<String>)x)).map(Long::parseLong);
    }

    public Flux<ScoredValue<String>> queryAllWithScore() {
        RedisSortedSetReactiveCommands<String, String> reactive = connection.reactive();
        Flux<ScoredValue<String>> zrangeAll = reactive.zrangeWithScores(tableName, 0, -1);
        return zrangeAll;
    }
}
