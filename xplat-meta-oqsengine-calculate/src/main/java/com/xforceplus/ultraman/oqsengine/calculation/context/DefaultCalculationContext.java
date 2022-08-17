package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Hint;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntitys;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 字段计算器上下文.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:18
 * @since 1.8
 */
public class DefaultCalculationContext implements CalculationContext {

    private IEntity sourceEntity;
    private IEntity focusEntity;
    private IEntity maintenanceEntity;
    private IEntityClass focusEntityClass;
    private IEntityField focusField;
    private CalculationScenarios scenarios;
    private Transaction transaction;
    private MetaManager metaManager;
    private MasterStorage masterStorage;
    private EventBus eventBus;
    private BizIDGenerator bizIDGenerator;
    private KeyValueStorage keyValueStorage;
    private TaskCoordinator taskCoordinator;
    private ExecutorService taskExecutorService;
    private Collection<Hint> hints;
    private ResourceLocker resourceLocker;
    private CalculationLogicFactory calculationLogicFactory;
    private ConditionsSelectStorage conditionsSelectStorage;
    // key为entityId.
    private Map<Long, IEntity> entityCache;
    // key为 entityId-fieldId的组合.
    private Map<String, ValueChange> valueChanges;

    private InfuenceGraph infuenceGraph;
    private Set<Long> lockedEnittyIds;
    private boolean maintenance;
    private long lockTimeoutMs;

    @Override
    public CalculationScenarios getScenariso() {
        return this.scenarios;
    }

    @Override
    public Optional<Transaction> getCurrentTransaction() {
        return Optional.ofNullable(transaction);
    }

    @Override
    public Optional<MasterStorage> getMasterStorage() {
        return Optional.ofNullable(this.masterStorage);
    }

    @Override
    public Optional<MetaManager> getMetaManager() {
        return Optional.ofNullable(this.metaManager);
    }

    @Override
    public Optional<KeyValueStorage> getKvStorage() {
        return Optional.ofNullable(this.keyValueStorage);
    }

    @Override
    public Optional<EventBus> getEvnetBus() {
        return Optional.ofNullable(this.eventBus);
    }

    @Override
    public Optional<TaskCoordinator> getTaskCoordinator() {
        return Optional.ofNullable(this.taskCoordinator);
    }

    @Override
    public IEntity getSourceEntity() {
        return this.sourceEntity;
    }

    @Override
    public IEntity getFocusEntity() {
        return this.focusEntity;
    }

    @Override
    public IEntityClass getFocusClass() {
        return this.focusEntityClass;
    }

    @Override
    public IEntityField getFocusField() {
        return this.focusField;
    }

    @Override
    public InfuenceGraph getInfuenceGraph() {
        return this.infuenceGraph;
    }

    @Override
    public void addInfuenceGraph(InfuenceGraph infuenceGraph) {
        this.infuenceGraph = infuenceGraph;
    }

    @Override
    public boolean isMaintenance() {
        return this.maintenance;
    }

    @Override
    public void startMaintenance(IEntity triggerEntity) {
        this.maintenance = true;

        this.maintenanceEntity = triggerEntity;
    }

    @Override
    public void stopMaintenance() {
        this.maintenance = false;

        this.maintenanceEntity = null;
    }

    @Override
    public Optional<IEntity> getMaintenanceTriggerEntity() {
        return Optional.ofNullable(this.maintenanceEntity);
    }

    @Override
    public void focusSourceEntity(IEntity entity) {
        this.sourceEntity = entity;
    }

    @Override
    public void focusEntity(IEntity entity, IEntityClass entityClass) {
        this.focusEntity = entity;
        this.focusEntityClass = entityClass;

        this.putEntityToCache(entity);
    }

    @Override
    public void focusField(IEntityField field) {
        this.focusField = field;
    }

    @Override
    public void focusTx(Transaction tx) {
        this.transaction = tx;
    }

    @Override
    public void addValueChange(ValueChange valueChange) {
        if (this.valueChanges == null) {
            this.valueChanges = new HashMap<>();
        }

        this.valueChanges.put(buildValueChangeKey(valueChange.getEntityId(), valueChange.getField().id()), valueChange);
    }

    @Override
    public Optional<ValueChange> getValueChange(IEntity entity, IEntityField field) {
        if (this.valueChanges == null) {
            return Optional.empty();
        } else {
            String key = buildValueChangeKey(entity.id(), field.id());
            return Optional.ofNullable(this.valueChanges.get(key));
        }
    }

    @Override
    public Collection<ValueChange> getValueChanges() {
        if (this.valueChanges == null) {
            return Collections.emptyList();
        } else {
            return this.valueChanges.values();
        }
    }

    @Override
    public void removeValueChange(IEntity entity, IEntityField field) {
        if (this.valueChanges != null) {

            String key = buildValueChangeKey(entity.id(), field.id());
            this.valueChanges.remove(key);
        }
    }

    @Override
    public void putEntityToCache(IEntity entity) {
        if (this.entityCache == null) {
            this.entityCache = new HashMap<>();
        }

        this.entityCache.put(entity.id(), entity);
    }

    @Override
    public Optional<IEntity> getEntityToCache(long entityId) {
        if (this.entityCache == null) {
            return Optional.empty();
        } else {

            return Optional.ofNullable(this.entityCache.get(entityId));
        }
    }

    @Override
    public void removeEntityFromCache(long entityId) {
        if (this.entityCache != null) {
            this.entityCache.remove(entityId);
        }
    }

    @Override
    public Collection<IEntity> getEntitiesFormCache() {
        if (this.entityCache == null) {
            return Collections.emptyList();
        } else {

            return this.entityCache.values();
        }
    }

    @Override
    public Optional<CalculationLogicFactory> getCalculationLogicFactory() {
        return Optional.ofNullable(this.calculationLogicFactory);
    }

    @Override
    public Optional<ConditionsSelectStorage> getConditionsSelectStorage() {
        return Optional.ofNullable(this.conditionsSelectStorage);
    }

    @Override
    public Optional<BizIDGenerator> getBizIDGenerator() {
        return Optional.ofNullable(this.bizIDGenerator);
    }

    @Override
    public Optional<ExecutorService> getTaskExecutorService() {
        return Optional.ofNullable(taskExecutorService);
    }

    @Override
    public Optional<ResourceLocker> getResourceLocker() {
        return Optional.ofNullable(resourceLocker);
    }

    @Override
    public void hint(IEntityField field, String hint) {
        if (this.hints == null) {
            this.hints = new LinkedList<>();
        }

        this.hints.add(new Hint(field, hint));
    }

    @Override
    public void hint(Hint hint) {
        if (this.hints == null) {
            this.hints = new LinkedList<>();
        }

        this.hints.add(hint);
    }

    @Override
    public Collection<Hint> getHints() {
        if (this.hints == null) {
            return Collections.emptyList();
        } else {
            return this.hints;
        }
    }

    @Override
    public boolean persist() {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {

            List<IEntity> entities = this.getEntitiesFormCache()
                .stream().filter(e -> e.isDirty()).collect(Collectors.toList());

            if (entities.isEmpty()) {
                return true;
            }

            MetaManager metaManager = getResourceWithEx(() -> getMetaManager());

            EntityPackage entityPackage = null;
            IEntity[] unsuccessfulEntities;
            for (int i = 0; i < entities.size(); i++) {

                if (entityPackage == null) {
                    entityPackage = new EntityPackage();
                }

                Optional<IEntityClass> entityClassOp = metaManager.load(entities.get(i).entityClassRef());
                if (!entityClassOp.isPresent()) {
                    throw new CalculationException(
                        String.format("Not found entityClass.[%s]", entities.get(i).entityClassRef().getId()));
                }
                /*
                使用非严格模式.
                如果 entity.entityClassRef 找到的 EntityClass 中的 profile 不一致将忽略,使用原始的EntityClass实例.
                 */
                entityPackage.put(entities.get(i), entityClassOp.get(), false);

                if (entityPackage.isFull()) {
                    try {
                        unsuccessfulEntities = doPersist(entityPackage);
                    } catch (SQLException ex) {
                        throw new CalculationException(ex.getMessage(), ex);
                    }

                    // 没有成功,产生了冲突.有其他事务更新了目标.
                    if (unsuccessfulEntities.length > 0) {
                        return false;
                    }
                }
            }

            // 剩余的.
            if (entityPackage != null && !entityPackage.isEmpty()) {
                try {
                    unsuccessfulEntities = doPersist(entityPackage);
                } catch (SQLException ex) {
                    throw new CalculationException(ex.getMessage(), ex);
                }

                if (unsuccessfulEntities.length > 0) {
                    return false;
                }
            }

        } finally {


            sample.stop(Timer.builder(MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS)
                .tags(
                    "logic", "all",
                    "action", "persist",
                    "exception", "none"
                )
                .publishPercentileHistogram(false)
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(Metrics.globalRegistry));
        }

        return true;
    }

    /**
     * 对于指定实例进行加锁.
     * 已经加锁过的不会再次进行加锁.
     */
    @Override
    public boolean tryLocksEntity(long... entityIds) {
        if (this.lockedEnittyIds == null) {
            this.lockedEnittyIds = new HashSet<>();
        }

        // 只保留没有加过锁的.
        String[] keys = Arrays.stream(entityIds)
            .filter(id -> !this.lockedEnittyIds.contains(id))
            .mapToObj(id -> IEntitys.resource(id)).toArray(String[]::new);

        if (keys.length > 0) {
            boolean result = false;
            try {
                result = this.resourceLocker.tryLocks(lockTimeoutMs, keys);
            } catch (InterruptedException e) {
                // donothing
            }

            if (result) {
                for (long id : entityIds) {
                    this.lockedEnittyIds.add(id);
                }
            }

            return result;
        } else {

            return true;

        }
    }

    @Override
    public long getLockTimeoutMs() {
        return this.lockTimeoutMs;
    }

    public Set<Long> getLockedEnittyIds() {
        return new HashSet<>(lockedEnittyIds);
    }

    @Override
    public void destroy() {
        if (lockedEnittyIds != null && !lockedEnittyIds.isEmpty()) {

            String[] keys = lockedEnittyIds.stream().map(id -> IEntitys.resource(id)).toArray(String[]::new);

            this.resourceLocker.unlocks(keys);
        }
    }

    /**
     * TODO: 没有处理事务累加器,被动修改的对象现在不会出现在事务累加器中. by dongbin 2021/11/18
     * 返回未成功的实例.
     */
    private IEntity[] doPersist(EntityPackage entityPackage) throws SQLException {
        MasterStorage masterStorage = getResourceWithEx(() -> getMasterStorage());

        masterStorage.replace(entityPackage);
        return entityPackage.stream().filter(e -> e.getKey().isDirty()).map(e -> e.getKey()).toArray(IEntity[]::new);
    }

    @Override
    public CalculationContext copy() {
        DefaultCalculationContext newContext = new DefaultCalculationContext();
        if (this.valueChanges != null) {
            newContext.valueChanges = new HashMap<>(this.valueChanges);
        }
        if (this.entityCache != null) {
            newContext.entityCache = new HashMap<>(this.entityCache);
        }
        newContext.maintenance = this.maintenance;
        newContext.sourceEntity = this.sourceEntity;
        newContext.focusEntity = this.focusEntity;
        newContext.focusField = this.focusField;
        newContext.focusEntityClass = this.focusEntityClass;
        newContext.eventBus = this.eventBus;
        newContext.transaction = this.transaction;
        newContext.scenarios = this.scenarios;
        newContext.metaManager = this.metaManager;
        newContext.masterStorage = this.masterStorage;
        newContext.bizIDGenerator = this.bizIDGenerator;
        newContext.keyValueStorage = this.keyValueStorage;
        newContext.taskCoordinator = this.taskCoordinator;
        newContext.taskExecutorService = this.taskExecutorService;
        newContext.resourceLocker = this.resourceLocker;
        newContext.conditionsSelectStorage = this.conditionsSelectStorage;
        return newContext;
    }

    private String buildValueChangeKey(long entityId, long fieldId) {
        return String.join("-", Long.toString(entityId), Long.toString(fieldId));
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private long lockTimeoutMs = 30000;
        private EventBus eventBus;
        private Transaction transaction;
        private CalculationScenarios scenarios;
        private MetaManager metaManager;
        private MasterStorage masterStorage;
        private BizIDGenerator bizIDGenerator;
        private KeyValueStorage keyValueStorage;
        private TaskCoordinator taskCoordinator;
        private ExecutorService taskExecutorService;
        private ResourceLocker resourceLocker;
        private ConditionsSelectStorage conditionsSelectStorage;
        private CalculationLogicFactory calculationLogicFactory;

        private Builder() {
        }

        public static Builder anCalculationContext() {
            return new Builder();
        }

        public Builder withScenarios(CalculationScenarios scenarios) {
            this.scenarios = scenarios;
            return this;
        }

        public Builder withMetaManager(MetaManager metaManager) {
            this.metaManager = metaManager;
            return this;
        }

        public Builder withMasterStorage(MasterStorage masterStorage) {
            this.masterStorage = masterStorage;
            return this;
        }

        public Builder withKeyValueStorage(KeyValueStorage keyValueStorage) {
            this.keyValueStorage = keyValueStorage;
            return this;
        }

        public Builder withTaskCoordinator(TaskCoordinator taskCoordinator) {
            this.taskCoordinator = taskCoordinator;
            return this;
        }

        public Builder withBizIDGenerator(BizIDGenerator bizIDGenerator) {
            this.bizIDGenerator = bizIDGenerator;
            return this;
        }

        public Builder withTransaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder withConditionsSelectStorage(ConditionsSelectStorage conditionsSelectStorage) {
            this.conditionsSelectStorage = conditionsSelectStorage;
            return this;
        }

        public Builder withCalculationLogicFactory(CalculationLogicFactory calculationLogicFactory) {
            this.calculationLogicFactory = calculationLogicFactory;
            return this;
        }

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Builder withTaskExecutorService(ExecutorService taskExecutorService) {
            this.taskExecutorService = taskExecutorService;
            return this;
        }

        public Builder withResourceLocker(ResourceLocker resourceLocker) {
            this.resourceLocker = resourceLocker;
            return this;
        }

        public Builder withLockTimeroutMs(long lockTimeoutMs) {
            this.lockTimeoutMs = lockTimeoutMs;
            return this;
        }

        /**
         * 构造.
         */
        public DefaultCalculationContext build() {
            DefaultCalculationContext defaultCalculationContext = new DefaultCalculationContext();
            defaultCalculationContext.eventBus = this.eventBus;
            defaultCalculationContext.taskCoordinator = this.taskCoordinator;
            defaultCalculationContext.taskExecutorService = this.taskExecutorService;
            defaultCalculationContext.masterStorage = this.masterStorage;
            defaultCalculationContext.metaManager = this.metaManager;
            defaultCalculationContext.keyValueStorage = this.keyValueStorage;
            defaultCalculationContext.scenarios = this.scenarios;
            defaultCalculationContext.bizIDGenerator = this.bizIDGenerator;
            defaultCalculationContext.transaction = this.transaction;
            defaultCalculationContext.conditionsSelectStorage = this.conditionsSelectStorage;
            defaultCalculationContext.resourceLocker = this.resourceLocker;
            defaultCalculationContext.calculationLogicFactory = this.calculationLogicFactory;
            defaultCalculationContext.lockTimeoutMs = this.lockTimeoutMs;
            return defaultCalculationContext;
        }
    }
}
