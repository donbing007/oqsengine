package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Comparator;

/**
 * s
 */
public class EntityRefComparator implements Comparator<EntityRef> {

    private FieldType fieldType;

    private StorageStrategyFactory storageStrategyFactory;

    public EntityRefComparator(FieldType fieldType, StorageStrategyFactory storageStrategyFactory) {
        this.fieldType = fieldType;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public int compare(EntityRef o1, EntityRef o2) {
        StorageStrategy strategy = storageStrategyFactory.getStrategy(fieldType);

        if(strategy.isMultipleStorageValue()){
            //TODO

        } else {
            //
        }

        //TODO combine string
        String sortValueA = o1.getOrderValue();
        String sortValueB = o2.getOrderValue();

        return fieldType.compare(sortValueA, sortValueB);
    }
}
