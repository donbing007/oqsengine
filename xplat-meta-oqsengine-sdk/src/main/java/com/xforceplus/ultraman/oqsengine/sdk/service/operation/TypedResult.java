package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;

import java.util.Optional;

/**
 * @author admin
 */
public interface TypedResult extends ResultSideOperationHandler {

    FieldType acceptType();

    @Override
    default Object apply(IEntityField source, Object src, OperationType token) {
        if (token == OperationType.RESULT) {
            return Optional.ofNullable(src)
                    .filter(x -> source.type() == acceptType())
                    .flatMap(nonNullSrc ->
                            source.type()
                                    .toTypedValue(source, nonNullSrc.toString()))
                    .map(IValue::getValue)
                    .orElse(src);
        }

        return src;
    }

    @Override
    default int compareTo(FieldOperationHandler fieldOperationHandler) {
        return 0;
    }
}
