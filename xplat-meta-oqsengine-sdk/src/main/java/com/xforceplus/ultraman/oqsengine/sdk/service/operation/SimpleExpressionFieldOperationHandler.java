package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;

/**
 * context service field
 */
public class SimpleExpressionFieldOperationHandler implements FieldOperationHandler {


    private ContextService contextService;

    public SimpleExpressionFieldOperationHandler(ContextService contextService) {
        this.contextService = contextService;
    }

    @Override
    public boolean require(IEntityField field, Object obj) {
        return obj instanceof String && obj.toString().startsWith("{{") && obj.toString().endsWith("}}");
    }

    @Override
    public Object onCreate(IEntityField field, Object o) {
        String key = getKey(o.toString());
        return calculateValFromString(key);
    }

    @Override
    public Object onUpdate(IEntityField field, Object o) {
        String key = getKey(o.toString());
        return calculateValFromString(key);
    }

    @Override
    public Object onUnHandle(IEntityField field, Object o) {
        return null;
    }

    private Object calculateValFromString(String key) {
        try {
            ContextKeys.LongKeys longKey = ContextKeys.LongKeys.valueOf(key.toUpperCase());
            return contextService.get(longKey);
        } catch (Exception ex) {

        }


        try {
            ContextKeys.StringKeys stringKeys = ContextKeys.StringKeys.valueOf(key.toUpperCase());
            return contextService.get(stringKeys);
        } catch (Exception ex) {

        }

        return null;
    }

    private String getKey(String obj) {
        return obj.substring(2, obj.length() - 2);
    }
}
