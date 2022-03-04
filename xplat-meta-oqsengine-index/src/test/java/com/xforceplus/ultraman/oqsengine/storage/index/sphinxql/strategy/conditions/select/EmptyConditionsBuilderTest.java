package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.EmptyConditionsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * EmptyConditionsBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 11/19/2020
 * @since <pre>Nov 19, 2020</pre>
 */
public class EmptyConditionsBuilderTest {

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityClass entityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withCode("test")
        .withField(longField)
        .build();

    @Test
    public void testBuild() throws Exception {

        EmptyConditionsBuilder emptyConditionsBuilder = new EmptyConditionsBuilder();
        String where = emptyConditionsBuilder.build(Conditions.buildEmtpyConditions(), entityClass).toString();

        Assertions.assertTrue(where.isEmpty());
    }
} 
