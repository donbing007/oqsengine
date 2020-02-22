package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * NoOrNorRanageConditionsBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/22/2020
 * @since <pre>Feb 22, 2020</pre>
 */
public class NoOrNorRanageConditionsBuilderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(Conditions conditions)
     */
    @Test
    public void testBuild() throws Exception {
        NoOrNorRanageConditionsBuilder builder = new NoOrNorRanageConditionsBuilder();
        buildCase().stream().forEach(c -> {
            String where = builder.build(c.conditions);
            Assert.assertEquals(c.expected, where);
        });
    }

    private List<Case> buildCase() {
        String expectPrefix = "MATCH('@" + FieldDefine.FULL_FIELDS + " ";
        String expectAfter = "')";
        return Arrays.asList(
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)
                    )
                ),
                expectPrefix + "=F1100" + expectAfter
            )
            ,
            new Case(
                new Conditions(
                    new Condition(
                        new Field(1, "c1", FieldType.LONG),
                        ConditionOperator.EQUALS,
                        new LongValue(new Field(1, "c1", FieldType.LONG), 100L)))
                    .addAnd(new Condition(
                        new Field(2, "c2", FieldType.STRING),
                        ConditionOperator.EQUALS,
                        new StringValue(new Field(2, "c2", FieldType.STRING), "test"))),
                expectPrefix + "=F1100 =F2test" + expectAfter
            )
        );
    }

    class Case {
        private Conditions conditions;
        private String expected;

        public Case(Conditions conditions, String expected) {
            this.conditions = conditions;
            this.expected = expected;
        }
    }

} 
