package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.springframework.core.Ordered;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TENANTID_KEY;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.USERNAME;

/**
 * handle all system operation
 */
public class FixedDefaultSystemOperationHandler implements FieldOperationHandler {

    private Map<String, Supplier<Object>> fixed = new HashMap<>();

    private static final Set<String> updateFields = Sets.newHashSet("update_time", "update_user_id", "update_user_name");

    public FixedDefaultSystemOperationHandler(ContextService contextService) {

        fixed.put("tenant_id", () -> contextService.get(TENANTID_KEY));
        fixed.put("create_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        fixed.put("create_user_name", () -> contextService.get(USERNAME));
        fixed.put("create_user_id", () -> contextService.get(ID));
        fixed.put("delete_flag", () -> "1");
        fixed.put("update_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        fixed.put("update_user_id", () -> contextService.get(ID));
        fixed.put("update_user_name", () -> contextService.get(USERNAME));
    }

    /**
     * make this always the end
     *
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean require(IEntityField field, Object obj) {
        return isSystemDefaultField(field);
    }

    @Override
    public Object onCreate(IEntityField field, Object o) {
        return fixed.get(field.name()).get();
    }

    @Override
    public Object onUpdate(IEntityField field, Object o) {
        if (updateFields.contains(field.name())) {
            return fixed.get(field.name()).get();
        }
        return null;
    }

    @Override
    public Object onUnHandle(IEntityField field, Object o) {
        //clear all
        return null;
    }

    private boolean isSystemDefaultField(IEntityField field) {
        String fieldName = field.name();
        return fixed.containsKey(fieldName);
    }
}
