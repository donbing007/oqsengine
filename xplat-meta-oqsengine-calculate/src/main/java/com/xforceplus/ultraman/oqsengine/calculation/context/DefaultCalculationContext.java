package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
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

/**
 * 字段计算器上下文.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:18
 * @since 1.8
 */
public class DefaultCalculationContext implements CalculationContext {

    private IEntity focusEntity;
    private IEntityClass focusEntityClass;
    private IEntityField focusField;
    private CalculationScenarios scenarios;
    private Transaction transaction;
    private MetaManager metaManager;
    private MasterStorage masterStorage;
    private BizIDGenerator bizIDGenerator;
    private KeyValueStorage keyValueStorage;
    private TaskCoordinator taskCoordinator;
    private Collection<CalculationHint> hints;
    private CalculationLogicFactory calculationLogicFactory;
    private ConditionsSelectStorage combindedSelectStorage;
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
    public Optional<TaskCoordinator> getTaskCoordinator() {
        return Optional.ofNullable(this.taskCoordinator);
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
    public void focusEntity(IEntity entity, IEntityClass entityClass) {
        this.focusEntity = entity;
        this.focusEntityClass = entityClass;
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
    public Optional<ConditionsSelectStorage> getCombindStorage() {
        return Optional.ofNullable(this.combindedSelectStorage);
    }

    @Override
    public Optional<BizIDGenerator> getBizIDGenerator() {
        return Optional.ofNullable(this.bizIDGenerator);
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
        return this.hints;
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private Transaction transaction;
        private CalculationScenarios scenarios;
        private MetaManager metaManager;
        private MasterStorage masterStorage;
        private BizIDGenerator bizIDGenerator;
        private KeyValueStorage keyValueStorage;
        private TaskCoordinator taskCoordinator;
        private ConditionsSelectStorage combindedSelectStorage;
        private Collection<ValueChange> valueChanges;

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

        public Builder withValueChanges(Collection<ValueChange> valueChanges) {
            this.valueChanges = valueChanges;
            return this;
        }

        public Builder withCombindedSelectStorage(ConditionsSelectStorage combindedSelectStorage) {
            this.combindedSelectStorage = combindedSelectStorage;
            return this;
        }

        /**
         * 构造.
         */
        public DefaultCalculationContext build() {
            DefaultCalculationContext defaultCalculationContext = new DefaultCalculationContext();
            defaultCalculationContext.taskCoordinator = this.taskCoordinator;
            defaultCalculationContext.masterStorage = this.masterStorage;
            defaultCalculationContext.metaManager = this.metaManager;
            defaultCalculationContext.keyValueStorage = this.keyValueStorage;
            defaultCalculationContext.scenarios = this.scenarios;
            defaultCalculationContext.bizIDGenerator = this.bizIDGenerator;
            defaultCalculationContext.transaction = this.transaction;
            defaultCalculationContext.combindedSelectStorage = this.combindedSelectStorage;
            return defaultCalculationContext;
        }
    }
}
