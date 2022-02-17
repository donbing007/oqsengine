package com.xforceplus.ultraman.oqsengine.core.service.impl.help;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.function.GetIDFunction;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.UnknownCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.SpringContextUtil;
import com.xforceplus.ultraman.oqsengine.calculation.utils.aviator.AviatorHelper;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.RedisOrderContinuousLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockIDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import io.lettuce.core.RedisClient;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import org.junit.jupiter.api.Disabled;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 测试帮助类.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
@Disabled
public class TestInitTools {
    private static BizIDGenerator bizIDGenerator;
    private static IdGenerator redisIDGenerator;

    /**
     * id生成.
     *
     * @param bizType 业务id类型.
     */
    public static void bizIdGenerator(String bizType) throws IllegalAccessException {
        AviatorHelper.addFunction(new GetIDFunction());
        RedisClient redisClient = CommonInitialization.getInstance().getRedisClient();
        redisIDGenerator = new RedisOrderContinuousLongIdGenerator(redisClient);
        MockedStatic mocked = mockStatic(SpringContextUtil.class);
        mocked.when(() -> SpringContextUtil.getBean(anyString())).thenReturn(redisIDGenerator);
        bizIDGenerator = toBizIdGenerator(bizType);
    }

    /**
     * 构造测试主体.
     */
    public static EntityManagementServiceImpl entityManagementService(MetaManager metaManager)
        throws IllegalAccessException, SQLException {
        RedisOrderContinuousLongIdGenerator redisIDGenerator = redisIDGenerator();

        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "longContinuousPartialOrderIdGenerator", redisIDGenerator);
        ReflectionTestUtils.setField(impl, "longNoContinuousPartialOrderIdGenerator", idGenerator());
        ReflectionTestUtils.setField(impl, "transactionExecutor", new MockTransactionExecutor());
        ReflectionTestUtils.setField(impl, "bizIDGenerator", bizIDGenerator);
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        ReflectionTestUtils.setField(impl, "calculation", new MockCalculation());

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
        InitializationHelper.destroy();
        InitializationHelper.clearAll();
    }


    private static BizIDGenerator toBizIdGenerator(String bizType) {
        MockIDGeneratorFactory.MockIDGenerator idGenerator = new MockIDGeneratorFactory.MockIDGenerator(bizType);

        IDGeneratorFactory idGeneratorFactory = new IDGeneratorFactory() {
            @Override
            public IDGenerator getIdGenerator(String bizType) {
                return idGenerator;
            }
        };

        BizIDGenerator bizIDGenerator = new BizIDGenerator();
        ReflectionTestUtils.setField(bizIDGenerator, "idGeneratorFactory", idGeneratorFactory);

        return bizIDGenerator;
    }

    private static RedisOrderContinuousLongIdGenerator redisIDGenerator() throws IllegalAccessException {

        RedisClient redisClient = CommonInitialization.getInstance().getRedisClient();

        RedisOrderContinuousLongIdGenerator redisIDGenerator = new RedisOrderContinuousLongIdGenerator(redisClient);

        return redisIDGenerator;
    }

    private static CalculationLogicFactory calculationLogicFactory() throws IllegalAccessException {
        return new CalculationLogicFactory() {
            @Override
            public CalculationLogic getCalculationLogic(CalculationType type) {
                return UnknownCalculationLogic.getInstance();
            }

            @Override
            public Collection<CalculationLogic> getCalculationLogics() {
                return Collections.emptyList();
            }
        };
    }

    public static LongIdGenerator idGenerator() {
        return new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));
    }

    static class MockCalculation implements Calculation {

        @Override
        public IEntity calculate(CalculationContext context) throws CalculationException {
            return context.getFocusEntity();
        }

        @Override
        public void maintain(CalculationContext context) throws CalculationException {

        }
    }

    static class MockTransactionExecutor implements TransactionExecutor {

        @Override
        public Object execute(ResourceTask storageTask) throws SQLException {
            try {
                return storageTask.run(
                    MultiLocalTransaction.Builder.anMultiLocalTransaction()
                        .withId(1)
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
            } catch (Exception e) {
                throw new SQLException(String.format("execute failed, message : %s", e.getMessage()));
            }
        }
    }
}
