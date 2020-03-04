package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.combine;

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

    public static Optional<IEntityField> findFieldByCode(IEntityClass entityClass, String code) {

        Optional<IEntityField> entityFieldOp = entityClass.field(code);
        Optional<IEntityField> entityClassFromParent = Optional.ofNullable(entityClass.extendEntityClass())
                .flatMap(parent -> parent.field(code));

        return combine(entityFieldOp, entityClassFromParent);
    }
}
