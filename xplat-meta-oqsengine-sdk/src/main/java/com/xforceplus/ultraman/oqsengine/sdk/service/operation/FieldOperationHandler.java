package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;

/**
 * an operation on field
 */
public interface FieldOperationHandler extends Comparable<FieldOperationHandler>
    , TriFunction<IEntityField, Object, OperationType, Object> {

    default int getOrder() {
        return 0;
    }

    boolean require(IEntityField field, Object obj);

    Object onCreate(IEntityField field, Object o);

    Object onUpdate(IEntityField field, Object o);

    default Object onReplace(IEntityField field, Object o) {
        return onUpdate(field, o);
    }

    default Object onUnHandle(IEntityField field, Object o) {
        return null;
    }

    @Override
    default Object apply(IEntityField field, Object o, OperationType phase) {
        if (require(field, o)) {
            if (phase == OperationType.CREATE) {
                return onCreate(field, o);
            } else if (phase == OperationType.UPDATE) {
                return onUpdate(field, o);
            } else if (phase == OperationType.REPLACE) {
                return onReplace(field, o);
            } else {
                return onUnHandle(field, o);
            }
        }
        return o;
    }

    @Override
    default int compareTo(FieldOperationHandler fieldOperationHandler) {
        return Integer.compare(getOrder(), fieldOperationHandler.getOrder());
    }
}
