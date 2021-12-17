package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.lock.MultiResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 计算字段触发场景.
 *
 * @author dongbin
 * @version 0.1 2021/08/19 15:17
 * @since 1.8
 */
public class DefaultCalculationContextTest {

    @Test
    public void testClone() throws Exception {
        MetaManager metaManager = Mockito.mock(MetaManager.class);
        MultiResourceLocker multiResourceLocker = Mockito.mock(MultiResourceLocker.class);
        ResourceLocker resourceLocker = Mockito.mock(ResourceLocker.class);
        ExecutorService executorService = Mockito.mock(ExecutorService.class);
        EventBus eventBus = Mockito.mock(EventBus.class);
        KeyValueStorage keyValueStorage = Mockito.mock(KeyValueStorage.class);
        TaskCoordinator taskCoordinator = Mockito.mock(TaskCoordinator.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        BizIDGenerator bizIDGenerator = Mockito.mock(BizIDGenerator.class);
        MasterStorage masterStorage = Mockito.mock(MasterStorage.class);
        ConditionsSelectStorage conditionsSelectStorage = Mockito.mock(ConditionsSelectStorage.class);
        CalculationScenarios scenarios = CalculationScenarios.REPLACE;

        DefaultCalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMultiResourceLocker(multiResourceLocker)
            .withResourceLocker(resourceLocker)
            .withTaskExecutorService(executorService)
            .withEventBus(eventBus)
            .withKeyValueStorage(keyValueStorage)
            .withTaskCoordinator(taskCoordinator)
            .withTransaction(transaction)
            .withBizIDGenerator(bizIDGenerator)
            .withMasterStorage(masterStorage)
            .withConditionsSelectStorage(conditionsSelectStorage)
            .withScenarios(scenarios).build();

        IEntityClass entityClass = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withCode("test")
            .withLevel(0)
            .build();

        IEntity entity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(entityClass.ref())
            .build();

        context.focusEntity(entity, entityClass);
        context.putEntityToCache(entity);

        CalculationContext cloneContext = (CalculationContext) context.clone();

        Assertions.assertSame(metaManager, cloneContext.getMetaManager().get());
        Assertions.assertSame(multiResourceLocker, cloneContext.getMultiResourceLocker().get());
        Assertions.assertSame(resourceLocker, cloneContext.getResourceLocker().get());
        Assertions.assertSame(executorService, cloneContext.getTaskExecutorService().get());
        Assertions.assertSame(eventBus, cloneContext.getEvnetBus().get());
        Assertions.assertSame(keyValueStorage, cloneContext.getKvStorage().get());
        Assertions.assertSame(taskCoordinator, cloneContext.getTaskCoordinator().get());
        Assertions.assertSame(transaction, cloneContext.getCurrentTransaction().get());
        Assertions.assertSame(bizIDGenerator, cloneContext.getBizIDGenerator().get());
        Assertions.assertSame(masterStorage, cloneContext.getMasterStorage().get());
        Assertions.assertSame(conditionsSelectStorage, cloneContext.getConditionsSelectStorage().get());
        Assertions.assertSame(entity, cloneContext.getFocusEntity());
        Assertions.assertSame(entity, cloneContext.getEntitiesFormCache().stream().findFirst().get());
        Assertions.assertSame(entityClass, cloneContext.getFocusClass());
        Assertions.assertNull(cloneContext.getFocusField());
        Assertions.assertEquals(scenarios, cloneContext.getScenariso());
        Assertions.assertEquals(1, cloneContext.getEntitiesFormCache().size());


        Assertions.assertTrue(cloneContext.getValueChanges().isEmpty());

    }
}