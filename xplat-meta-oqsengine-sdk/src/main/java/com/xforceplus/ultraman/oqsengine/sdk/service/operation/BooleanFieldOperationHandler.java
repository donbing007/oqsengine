package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

/**
 * boolean field converter
 * @author admin
 */
public class BooleanFieldOperationHandler implements TypedResult {

    @Override
    public FieldType acceptType() {
        return FieldType.BOOLEAN;
    }
}
