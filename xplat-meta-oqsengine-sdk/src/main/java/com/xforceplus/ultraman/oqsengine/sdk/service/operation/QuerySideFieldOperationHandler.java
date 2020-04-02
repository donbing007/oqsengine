package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;

/**
 * query side field operation
 */
public interface QuerySideFieldOperationHandler extends FieldOperationHandler{

    Object onQuery(IEntityField field, Object o);

    @Override
    default Object apply(IEntityField field, Object o, OperationType phase) {
        if (require(field, o)) {
            if (phase == OperationType.CREATE) {
                return onCreate(field, o);
            } else if (phase == OperationType.UPDATE) {
                return onUpdate(field, o);
            } else if (phase == OperationType.REPLACE) {
                return onReplace(field, o);
            } else if (phase == OperationType.QUERY) {
                return onQuery(field, o);
            } else {
                return onUnHandle(field, o);
            }
        }
        return o;
    }

}
