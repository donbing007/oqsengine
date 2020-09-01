package com.xforceplus.ultraman.oqsengine.sdk.service.export;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Map;
import java.util.Optional;

/**
 * custom field value to String
 */
public interface ExportCustomFieldToString {

    boolean isSupport(IEntityClass entityClass, IEntityField field);

    Optional<String> getString(IEntityClass entityClass, IEntityField entityField, String value, Map<String, Object> context);

    default boolean isDefault(){
        return false;
    }
}
