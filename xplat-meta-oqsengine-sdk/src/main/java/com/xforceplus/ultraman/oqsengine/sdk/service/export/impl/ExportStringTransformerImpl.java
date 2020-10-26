package com.xforceplus.ultraman.oqsengine.sdk.service.export.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportCustomFieldToString;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportStringTransformer;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * string to transformer
 */
public class ExportStringTransformerImpl implements ExportStringTransformer {

    @Autowired
    private List<ExportCustomFieldToString> customTransformers;

    @Autowired
    private DefaultExportCustomFieldToString defaultExportCustomFieldToString;

    private Logger logger = LoggerFactory.getLogger(ExportStringTransformer.class);

    @Override
    public String toString(IEntityClass entityClass, IEntityField entityField, Object value, Map<String, Object> context) {

        String safeSourceValue = Optional.ofNullable(value).map(Object::toString).orElse("");

        String retStr = customTransformers.stream()
                .filter(x -> !x.isDefault() && x.isSupport(entityClass, entityField))
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("transform {}:{} with default", entityClass.code(), entityField.name());
                    return defaultExportCustomFieldToString;
                })
                .getString(entityClass, entityField, safeSourceValue, context)
                .orElse("");

        return StringEscapeUtils.escapeCsv("\t" + retStr);
    }
}
