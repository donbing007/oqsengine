package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * default filed operation
 * do nothing
 */
public class DefaultFieldOperationHandler implements FieldOperationHandler{

    @Override
    public boolean require(IEntityField field, Object obj) {
        return true;
    }

    @Override
    public Object onCreate(IEntityField field, Object o) {
        return o;

    }

    @Override
    public Object onUpdate(IEntityField field, Object o) {
        return o;
    }

    @Override
    public Object onUnHandle(IEntityField field, Object o) {
        return null;
    }
}
