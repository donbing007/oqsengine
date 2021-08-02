package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_HOST;
import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_PORT;

import com.xforceplus.ultraman.oqsengine.calculation.adapt.RedisIDGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockIDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class BaseInit {

    static RedissonClient redissonClient;

    public static EntityManagementServiceImpl entityManagementService(MetaManager metaManager)
        throws IllegalAccessException {
        RedisIDGenerator redisIDGenerator = redisIDGenerator();

        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "redisIDGenerator", redisIDGenerator);
        ReflectionTestUtils.setField(impl, "idGenerator", idGenerator());
        ReflectionTestUtils.setField(impl, "transactionExecutor", new MockTransactionExecutor());
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        ReflectionTestUtils.setField(impl, "eventBus", new EventBus() {
            @Override
            public void watch(EventType type, Consumer<Event> listener) {

            }

            @Override
            public void notify(Event event) {

            }
        });

        return impl;
    }

    public static void close() throws Exception {
        InitializationHelper.clearAll();
        if (null != redissonClient) {
            redissonClient.shutdown();
        }
    }

    private static RedisIDGenerator redisIDGenerator() throws IllegalAccessException {
        Config config = new Config();
        String redisIp = System.getProperty(REDIS_HOST);
        int redisPort = Integer.parseInt(System.getProperty(REDIS_PORT));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        RedissonClient redissonClient = Redisson.create(config);
        RedisIDGenerator redisIDGenerator = new RedisIDGenerator();
        Collection<Field> taskFields = ReflectionUtils.printAllMembers(redisIDGenerator);
        ReflectionUtils.reflectionFieldValue(taskFields,"redissonClient",redisIDGenerator,redissonClient);

        return redisIDGenerator;
    }

    public static LongIdGenerator idGenerator() {
        return new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));
    }


    public static class MockTransactionExecutor implements TransactionExecutor {

        @Override
        public Object execute(ResourceTask storageTask) throws SQLException {
            return storageTask.run(
                MultiLocalTransaction.Builder.anMultiLocalTransaction()
                    .withId(1)
                    .withCacheEventHandler(new DoNothingCacheEventHandler())
                    .withEventBus(
                        new EventBus() {
                            @Override
                            public void watch(EventType type, Consumer<Event> listener) {
                            }

                            @Override
                            public void notify(Event event) {
                            }
                        }
                    )
                    .build(),
                null,
                new DefaultExecutorHint());
        }
    }
}
