package com.xforceplus.ultraman.oqsengine.sdk.service.export;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Map;

/**
 * String transformer
 */
public interface ExportStringTransformer {

    String toString(IEntityClass entityClass, IEntityField field, Object value, Map<String, Object> context);

}
