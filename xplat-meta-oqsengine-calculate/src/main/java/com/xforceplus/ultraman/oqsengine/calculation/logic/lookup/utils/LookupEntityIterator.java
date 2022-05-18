package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.utils;

import com.xforceplus.ultraman.oqsengine.common.iterator.AbstractDataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.util.Collection;
import java.util.List;

/**
 * 对象迭代器.
 *
 * @author dongbin
 * @version 0.1 2021/11/24 14:30
 * @since 1.8
 */
public class LookupEntityIterator extends AbstractDataIterator<IEntity> {

    private ConditionsSelectStorage combinedSelectStorage;
    private MasterStorage masterStorage;
    private IEntityClass entityClass;
    private IEntityField field;
    private long targetEntityId;
    private long startId;

    public LookupEntityIterator(int buffSize) {
        super(buffSize);
    }

    public LookupEntityIterator(int buffSize, long maxSize) {
        super(buffSize, maxSize);
    }

    public ConditionsSelectStorage getCombinedSelectStorage() {
        return combinedSelectStorage;
    }

    public void setCombinedSelectStorage(ConditionsSelectStorage combinedSelectStorage) {
        this.combinedSelectStorage = combinedSelectStorage;
    }

    public MasterStorage getMasterStorage() {
        return masterStorage;
    }

    public void setMasterStorage(MasterStorage masterStorage) {
        this.masterStorage = masterStorage;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public IEntityField getField() {
        return field;
    }

    public void setField(IEntityField field) {
        this.field = field;
    }

    public long getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(long targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    @Override
    protected void load(List<IEntity> buff, int limit) throws Exception {
        Collection<EntityRef> refs = combinedSelectStorage.select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new AttachmentCondition(
                        this.field,
                        true,
                        Long.toString(this.targetEntityId)
                    )
                ).addAnd(
                    new Condition(
                        EntityField.ID_ENTITY_FIELD,
                        ConditionOperator.GREATER_THAN,
                        new LongValue(EntityField.ID_ENTITY_FIELD, startId)
                    )
                ),
            this.entityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(Page.newSinglePage(limit))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        if (!refs.isEmpty()) {
            long[] ids = refs.stream().mapToLong(r -> r.getId()).toArray();
            buff.addAll(masterStorage.selectMultiple(ids, this.entityClass));
            this.startId = ids[ids.length - 1];
        }
    }
}
