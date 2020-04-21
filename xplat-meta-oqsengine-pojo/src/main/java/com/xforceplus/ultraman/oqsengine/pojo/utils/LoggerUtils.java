package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 日志记录工具.
 */
public class LoggerUtils {

    private static final String TYPE_ERROR = "Type Err From %s to %s:[%s] with cause %s";

    public static String typeConverterError(IEntityField field, Object value, Throwable ex) {
        return String.format(TYPE_ERROR, value, field.name(), field.id(), ex.getMessage());
    }
}
