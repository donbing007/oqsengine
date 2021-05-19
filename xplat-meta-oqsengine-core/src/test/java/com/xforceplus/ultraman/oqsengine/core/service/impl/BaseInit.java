package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculate.ActualCalculateStorage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockIDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.UniqueMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class BaseInit {

    public static EntityManagementServiceImpl entityManagementService(MetaManager metaManager) {
        BizIDGenerator bizIDGenerator = new BizIDGenerator();
        ReflectionTestUtils.setField(bizIDGenerator, "idGeneratorFactory", new MockIDGeneratorFactory());

        EntityManagementServiceImpl impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "bizIDGenerator", bizIDGenerator);
        ReflectionTestUtils.setField(impl, "idGenerator", idGenerator());
        ReflectionTestUtils.setField(impl, "transactionExecutor", new MockTransactionExecutor());
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        ReflectionTestUtils.setField(impl, "calculateStorage", new ActualCalculateStorage());
        ReflectionTestUtils.setField(impl, "uniqueStorage", new MockUniqueMasterStorage());
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

    public static class MockUniqueMasterStorage implements UniqueMasterStorage {

        @Override
        public Optional<StorageUniqueEntity> select(List<BusinessKey> businessKeys, IEntityClass entityClass)
            throws SQLException {
            return Optional.empty();
        }

        @Override
        public boolean containUniqueConfig(List<BusinessKey> businessKeys, IEntityClass entityClass) {
            return false;
        }

        @Override
        public boolean containUniqueConfig(IEntity entity, IEntityClass entityClass) {
            return false;
        }
    }
}
