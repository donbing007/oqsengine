package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Entity helper.
 */
public class EntityHelper {

    /**
     * 构造IEntityClass实例.
     */
    public static IEntityClass toEntityClass(EntityUp entityUp, MetaManager metaManager) {

        boolean hasExtendedClass =
            entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.EXTENDENTITYCLASS_FIELD_NUMBER));

        Optional<IEntityClass> entityClassOp = metaManager.load(entityUp.getId());
        if (entityClassOp.isPresent()) {
            return entityClassOp.get();
        } else {
            throw new IllegalArgumentException(
                String.format("Meta information that does not exist.[%s]", entityUp.getCode()));
        }
    }


    /**
     * to typed value.
     */
    public static List<IValue> toTypedValue(IEntityField entityField, String value) {
        if (value != null && ValueWithEmpty.isEmpty(value)) {
            return Collections.singletonList(new EmptyTypedValue(entityField));
        } else {
            return entityField.type().toTypedValue(entityField, value)
                .map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
    }
}
