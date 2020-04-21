package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;

/**
 * result value converter
 */
public interface ResultSideOperationHandler  extends Comparable<FieldOperationHandler>
        , TriFunction<IEntityField, Object, OperationType, Object> {
}
