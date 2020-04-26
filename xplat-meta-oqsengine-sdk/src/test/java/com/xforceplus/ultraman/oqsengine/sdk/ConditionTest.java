package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.DefaultHandleValueService;
import com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Condition test
 */
public class ConditionTest {

    @Test
    public void testQueryNull() {
        String x = null;
        ConditionQueryRequest abc = new RequestBuilder()
                .field("abc", ConditionOp.eq, x)
                .build();

        //long id, String code, Collection<IEntityField> fields
        //long id, String name, FieldType fieldType
        EntityField field = new EntityField(1L, "abc", FieldType.STRING);
        EntityClass entityClass = new EntityClass(1L, "test", Collections.singleton(field));
        EntityClassToGrpcConverter.toSelectByCondition(entityClass, null, abc);
    }

    @Test
    public void testCreateNull() {
        String x = null;

        Map<String, Object> create = new HashMap<>();

        create.put("abc", null);

        EntityField field = new EntityField(1L, "abc", FieldType.STRING);
        EntityClass entityClass = new EntityClass(1L, "test", Collections.singleton(field));
        DefaultHandleValueService handleValueService = new DefaultHandleValueService(Collections.emptyList(), Collections.emptyList());

        List<ValueUp> valueUps = handleValueService.handlerValue(entityClass, create, OperationType.CREATE);
        EntityClassToGrpcConverter.toEntityUp(entityClass, null, valueUps);
    }
}
