package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import org.springframework.util.StringUtils;

/**
 * handle default
 */
public class DefaultFieldValueOperationHandler implements FieldOperationHandler {

    @Override
    public boolean require(IEntityField field, Object obj) {
        String dictId = field.dictId();
        String defaultValue = field.defaultValue();
        String type = field.type().name();

        if ("ENUM".equals(type)) {
            if (!StringUtils.isEmpty(dictId) && !StringUtils.isEmpty(defaultValue)) {
               return true;
            }
        } else if ("LONG".equals(type)) {
            if (!StringUtils.isEmpty(defaultValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object onCreate(IEntityField field, Object o) {
        if(o == null){

        }
        return o;
    }

    @Override
    public Object onUpdate(IEntityField field, Object o) {
        return null;
    }

    @Override
    public Object onUnHandle(IEntityField field, Object o) {
        return null;
    }
}
