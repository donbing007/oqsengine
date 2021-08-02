package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.RedisOrderContinuousLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import io.lettuce.core.RedisClient;
import java.sql.SQLException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Disabled;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
@Disabled
public class TestInitTools {

    /**
     * 构造测试主体.
     */
    public static EntityManagementServiceImpl entityManagementService(MetaManager metaManager)
        throws IllegalAccessException {
        RedisOrderContinuousLongIdGenerator redisIDGenerator = redisIDGenerator();

        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "longContinuousPartialOrderIdGenerator", redisIDGenerator);
        ReflectionTestUtils.setField(impl, "longNoContinuousPartialOrderIdGenerator", idGenerator());
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

    /**
     * 关闭.
     */
    public static void close() throws Exception {
        InitializationHelper.clearAll();
    }

    private static RedisOrderContinuousLongIdGenerator redisIDGenerator() throws IllegalAccessException {

        RedisClient redisClient = CommonInitialization.getInstance().getRedisClient();

        RedisOrderContinuousLongIdGenerator redisIDGenerator = new RedisOrderContinuousLongIdGenerator(redisClient);

        return redisIDGenerator;
    }

    public static LongIdGenerator idGenerator() {
        return new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));
    }


    static class MockTransactionExecutor implements TransactionExecutor {

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
