package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;

import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.sdk.util.OptionalHelper.*;

/**
 * a helper for entityClass
 */
public class IEntityClassHelper {

    /**
     * find field from entity fields and entityParent fields
     * @param entityClass
     * @param fieldId
     * @return
     */
    public static Optional<IEntityField> findFieldById(IEntityClass entityClass, long fieldId) {

        Optional<IEntityField> entityFieldOp = entityClass.field(fieldId);
        Optional<IEntityField> entityClassFromParent = Optional.ofNullable(entityClass.extendEntityClass())
                .flatMap(parent -> parent.field(fieldId));

        return combine(entityFieldOp, entityClassFromParent);
    }
}
