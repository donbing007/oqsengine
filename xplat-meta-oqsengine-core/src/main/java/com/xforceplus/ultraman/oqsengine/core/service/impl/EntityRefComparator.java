package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import java.util.Comparator;

/**
 * s
 */
public class EntityRefComparator implements Comparator<EntityRef> {

    private FieldType fieldType;

    public EntityRefComparator(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public int compare(EntityRef o1, EntityRef o2) {
        //TODO combine string
        String sortValueA = o1.getOrderValue();
        String sortValueB = o2.getOrderValue();

        return fieldType.compare(sortValueA, sortValueB);
    }
}
