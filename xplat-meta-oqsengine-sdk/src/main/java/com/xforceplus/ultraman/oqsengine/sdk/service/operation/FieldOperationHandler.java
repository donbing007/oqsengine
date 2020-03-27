package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * an operation on field
 */
public interface FieldOperationHandler extends Comparable<FieldOperationHandler>
        , TriFunction<IEntityField, Object, String, Object> {

    String CREATE = "CREATE";

    String UPDATE = "UPDATE";

    default int getOrder() {
        return 0;
    }

    boolean require(IEntityField field, Object obj);

    Object onCreate(IEntityField field, Object o);

    Object onUpdate(IEntityField field, Object o);

    Object onUnHandle(IEntityField field, Object o);

    @Override
    default Object apply(IEntityField field, Object o, String phase) {
        if (require(field, o)) {
            if (phase.equalsIgnoreCase(CREATE)) {
                return onCreate(field, o);
            } else if (phase.equalsIgnoreCase(UPDATE)) {
                return onUpdate(field, o);
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
