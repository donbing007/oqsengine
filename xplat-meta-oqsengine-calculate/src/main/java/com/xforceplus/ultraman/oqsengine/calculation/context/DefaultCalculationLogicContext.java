package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.calculation.IDGenerator;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * 计算默认上下文实例.
 *
 * @author dongbin
 * @version 0.1 2021/07/07 17:13
 * @since 1.8
 */
public class DefaultCalculationLogicContext implements CalculationLogicContext {

    private boolean build;
    private IEntity entity;
    private IEntityField field;
    private IEntityClass sourceEntityClass;
    private MasterStorage masterStorage;
    private MetaManager metaManager;
    private KeyValueStorage keyValueStorage;
    private Map<String, Object> attributes;
    private Collection<CalculationHint> hints;
    private IDGenerator bizIDGenerator;

    @Override
    public boolean isBuild() {
        return this.build;
    }

    @Override
    public boolean isReplace() {
        return !this.build;
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void focusField(IEntityField field) {
        this.field = field;
    }

    @Override
    public IEntityField getFocusField() {
        return this.field;
    }

    @Override
    public IEntityClass getEntityClass() {
        return this.sourceEntityClass;
    }

    @Override
    public MasterStorage getMasterStorage() {
        return this.masterStorage;
    }

    @Override
    public MetaManager getMetaManager() {
        return this.metaManager;
    }

    @Override
    public KeyValueStorage getKvStorage() {
        return this.keyValueStorage;
    }

    @Override
    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    @Override
    public void hint(String hint) {
        if (hint == null || hint.isEmpty()) {
            return;
        }

        if (hints == null) {
            hints = new LinkedList<>();
        }

        hints.add(new CalculationHint(field, hint));

    }

    @Override
    public Collection<CalculationHint> getHints() {
        if (null == hints) {
            return new ArrayList<>();
        }
        return new ArrayList<>(hints);
    }

    @Override
    public void bizIdGenerator(IDGenerator bizIDGenerator) {
        this.bizIDGenerator = bizIDGenerator;
    }

    @Override
    public IDGenerator getBizIDGenerator() {
        return bizIDGenerator;
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private boolean build;
        private IEntity entity;
        private IEntityClass entityClass;
        private MasterStorage masterStorage;
        private KeyValueStorage keyValueStorage;
        private MetaManager metaManager;
        private Map<String, Object> attributes;
        private IDGenerator bizIDGenerator;

        private Builder() {
        }

        public static Builder anCalculationLogicContext() {
            return new Builder();
        }

        public Builder withBuild(boolean build) {
            this.build = build;
            return this;
        }

        public Builder withEntity(IEntity entity) {
            this.entity = entity;
            return this;
        }

        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withMasterStorage(MasterStorage masterStorage) {
            this.masterStorage = masterStorage;
            return this;
        }

        public Builder withKeyValueStorage(KeyValueStorage kv) {
            this.keyValueStorage = kv;
            return this;
        }

        public Builder withMetaManager(MetaManager metaManager) {
            this.metaManager = metaManager;
            return this;
        }

        public Builder withBizIdGenerator(IDGenerator bizIdGenerator) {
            this.bizIDGenerator = bizIdGenerator;
            return this;
        }

        /**
         * 增加新的属性.
         */
        public Builder withAttribute(String key, Object value) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            this.attributes.put(key, value);
            return this;
        }

        /**
         * 构造实例.
         *
         * @return 实例.
         */
        public DefaultCalculationLogicContext build() {
            DefaultCalculationLogicContext defaultCalculationLogicContext = new DefaultCalculationLogicContext();
            defaultCalculationLogicContext.build = this.build;
            defaultCalculationLogicContext.entity = this.entity;
            defaultCalculationLogicContext.sourceEntityClass = this.entityClass;
            defaultCalculationLogicContext.attributes = this.attributes;
            defaultCalculationLogicContext.metaManager = this.metaManager;
            defaultCalculationLogicContext.masterStorage = this.masterStorage;
            defaultCalculationLogicContext.keyValueStorage = this.keyValueStorage;
            defaultCalculationLogicContext.bizIDGenerator = this.bizIDGenerator;
            return defaultCalculationLogicContext;
        }
    }
}
