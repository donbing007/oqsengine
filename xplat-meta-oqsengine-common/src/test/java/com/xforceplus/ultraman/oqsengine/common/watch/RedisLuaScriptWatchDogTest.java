package com.xforceplus.ultraman.oqsengine.common.watch;

import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dongbin
 * @version 0.1 2021/09/23 11:41
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class RedisLuaScriptWatchDogTest {

    final Logger logger = LoggerFactory.getLogger(RedisLuaScriptWatchDogTest.class);

    private RedisClient redisClient;
    private RedisLuaScriptWatchDog watchDog;

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        watchDog = new RedisLuaScriptWatchDog(redisClient, 200);
    }

    @After
    public void after() throws Exception {
        redisClient.connect().sync().flushall();
        redisClient.connect().sync().scriptFlush();
        redisClient.shutdown();
        redisClient = null;

        watchDog.destroy();
    }

    /**
     * 测试脚本不存在,新创建.
     */
    @Test
    public void testNewLua() throws Exception {
        String script = "return 'Hello World!'";

        String sha = watchDog.watch(script);

        try (StatefulRedisConnection<String, String> conn = redisClient.connect()) {

            RedisCommands<String, String> command = conn.sync();

            Assert.assertTrue(command.scriptExists(sha).get(0));

            String value = command.evalsha(sha, ScriptOutputType.VALUE);
            Assert.assertEquals("Hello World!", value);

        }
    }

    /**
     * 测试脚本丢失.
     */
    @Test
    public void testLossLua() throws Exception {
        String[] scripts = IntStream.range(0, 10).mapToObj(i -> String.format("return '%d'", i)).toArray(String[]::new);

        String[] shas = Arrays.stream(scripts).map(lua -> watchDog.watch(lua)).toArray(String[]::new);


        try (StatefulRedisConnection<String, String> conn = redisClient.connect()) {
            RedisCommands<String, String> command = conn.sync();
            Assert.assertEquals(shas.length, command.scriptExists(shas).stream().filter(b -> b).count());


            command.scriptFlush();


            int max = 100;
            int p = 0;
            while(command.scriptExists(shas).stream().filter(b -> b).count() < 10) {
                logger.info("Script did not resume, continue to wait.");

                TimeUnit.MILLISECONDS.sleep(1000L);

                if (p > max) {
                    Assert.fail(String.format(
                        "The script is still not restored after waiting %d milliseconds.", 1000 * 100));
                } else {

                    max++;
                }
            }

            String[] values = Arrays.stream(shas)
                .map(s -> command.evalsha(s, ScriptOutputType.VALUE)).toArray(String[]::new);

            for (int i = 0; i < 10; i++) {
                Assert.assertEquals(Integer.toString(i), values[i]);
            }
        }
    }
}