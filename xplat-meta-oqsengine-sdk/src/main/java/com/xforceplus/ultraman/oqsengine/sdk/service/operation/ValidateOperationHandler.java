package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

public class ValidateOperationHandler implements FieldOperationHandler {

    @Override
    public boolean require(IEntityField field, Object obj) {
        return true;
    }

    @Override
    public Object onCreate(IEntityField field, Object o) {
        return null;
    }

    @Override
    public Object onUpdate(IEntityField field, Object o) {
        return null;
    }

    @Override
    public Object onUnHandle(IEntityField field, Object o) {
        return null;
    }


}
