package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 字段计算器上下文.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:18
 * @since 1.8
 */
public class DefaultCalculationContext implements CalculationContext {

    private IEntity sourceEntity;
    private boolean maintenance;
    private IEntity focusEntity;
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
    private Collection<CalculationHint> hints;
    private CalculationLogicFactory calculationLogicFactory;
    private ConditionsSelectStorage conditionsSelectStorage;
    // key为entityId.
    private Map<Long, IEntity> entityCache;
    // key为 entityId-fieldId的组合.
    private Map<String, ValueChange> valueChanges;

    public DefaultCalculationContext() {
        calculationLogicFactory = new CalculationLogicFactory();
    }

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
    public boolean isMaintenance() {
        return this.maintenance;
    }

    @Override
    public void startMaintenance() {
        this.maintenance = true;
    }

    @Override
    public void stopMaintenance() {
        this.maintenance = false;
    }

    @Override
    public void focusEntity(IEntity entity, IEntityClass entityClass) {
        if (this.focusEntity == null) {
            this.sourceEntity = entity;
        }
        this.focusEntity = entity;
        this.focusEntityClass = entityClass;

        this.putEntityToCache(entity);
    }

    @Override
    public void focusField(IEntityField field) {
        this.focusField = field;
    }

    @Override
    public void addValueChange(ValueChange valueChange) {
        if (this.valueChanges == null) {
            this.valueChanges = new HashMap<>();
        }

        this.valueChanges.put(String.join("-",
            Long.toString(valueChange.getEntityId()), Long.toString(valueChange.getField().id())), valueChange);
    }

    @Override
    public Optional<ValueChange> getValueChange(IEntity entity, IEntityField field) {
        if (this.valueChanges == null) {
            return Optional.empty();
        } else {
            String key = String.join("-", Long.toString(entity.id()), Long.toString(field.id()));
            return Optional.ofNullable(this.valueChanges.get(key));
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
    public void hint(IEntityField field, String hint) {
        if (this.hints == null) {
            this.hints = new LinkedList<>();
        }

        this.hints.add(new CalculationHint(field, hint));
    }

    @Override
    public Collection<CalculationHint> getHints() {
        if (this.hints == null) {
            return Collections.emptyList();
        } else {
            return this.hints;
        }
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private EventBus eventBus;
        private Transaction transaction;
        private CalculationScenarios scenarios;
        private MetaManager metaManager;
        private MasterStorage masterStorage;
        private BizIDGenerator bizIDGenerator;
        private KeyValueStorage keyValueStorage;
        private TaskCoordinator taskCoordinator;
        private ExecutorService taskExecutorService;
        private ConditionsSelectStorage conditionsSelectStorage;

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

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Builder withTaskExecutorService(ExecutorService taskExecutorService) {
            this.taskExecutorService = taskExecutorService;
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
            return defaultCalculationContext;
        }
    }
}
