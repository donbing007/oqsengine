package com.xforceplus.ultraman.oqsengine.calculation.impl;

import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_HOST;
import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_PORT;

import com.xforceplus.ultraman.oqsengine.calculation.adapt.RedisIDGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/15/21 9:58 AM
 * @since 1.8
 */
@ExtendWith(RedisContainer.class)
public class RedisIDGeneratorTest {

    private RedissonClient redissonClient;
    private RedisIDGenerator redisIDGenerator;

    @BeforeEach
    public void before() throws IllegalAccessException {
        Config config = new Config();
        String redisIp = System.getProperty(REDIS_HOST);
        int redisPort = Integer.parseInt(System.getProperty(REDIS_PORT));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        redissonClient = Redisson.create(config);
        redisIDGenerator = new RedisIDGenerator();
        Collection<Field> taskFields = ReflectionUtils.printAllMembers(redisIDGenerator);
        ReflectionUtils.reflectionFieldValue(taskFields,"redissonClient",redisIDGenerator,redissonClient);
    }

    @Test
    public void testGenerator() {
       Long result =  redisIDGenerator.nextId("hello",1);
        Assert.assertEquals(result.longValue(),1l);
        for(int j=0;j<100;j++) {
            result =  redisIDGenerator.nextId("hello",1);
        }
        Assert.assertEquals(result.longValue(),101l);
    }

    @Test
    public void testGeneratorBatch() {
        List<Long> result =  redisIDGenerator.nextIds("hello1",1,100);
        Assert.assertEquals(result.size(),100);
        Assert.assertEquals(result.get(0).longValue(),1l);
        Assert.assertEquals(result.get(99).longValue(),100l);
    }
}
