package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.EmptyConditionsBuilder;
import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EmptyConditionsBuilder Tester.
 *
 * @author dongbin
 * @version 1.0 11/19/2020
 * @since <pre>Nov 19, 2020</pre>
 */
public class EmptyConditionsBuilderTest {

    final Logger logger = LoggerFactory.getLogger(EmptyConditionsBuilderTest.class);

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test", Arrays.asList(longField));

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(IEntityClass entityClass, Conditions conditions)
     */
    @Test
    public void testBuild() throws Exception {

        EmptyConditionsBuilder emptyConditionsBuilder = new EmptyConditionsBuilder();
        String where = emptyConditionsBuilder.build(Conditions.buildEmtpyConditions(), entityClass).toString();

        Assert.assertTrue(where.isEmpty());
    }


} 
