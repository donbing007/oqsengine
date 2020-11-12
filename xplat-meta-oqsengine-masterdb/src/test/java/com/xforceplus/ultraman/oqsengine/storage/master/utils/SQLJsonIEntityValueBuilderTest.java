package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SQLJsonIEntityValueBuilder Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/12/2020
 * @since <pre>Nov 12, 2020</pre>
 */
public class SQLJsonIEntityValueBuilderTest {

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING);
    private static IEntityField boolField = new EntityField(Long.MAX_VALUE - 2, "bool", FieldType.BOOLEAN);
    private static IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 3, "datetime", FieldType.DATETIME);
    private static IEntityField decimalField = new EntityField(Long.MAX_VALUE - 4, "decimal", FieldType.DECIMAL);
    private static IEntityField enumField = new EntityField(Long.MAX_VALUE - 5, "enum", FieldType.ENUM);
    private static IEntityField stringsField = new EntityField(Long.MAX_VALUE - 6, "strings", FieldType.STRINGS);

    private static StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
    private static Map<String, IEntityField> FIELDS_TABLE;

    static {
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        FIELDS_TABLE = new HashMap();
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE), longField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 1), stringField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 2), boolField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 3), dateTimeField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 4), decimalField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 5), enumField);
        FIELDS_TABLE.put(Long.toString(Long.MAX_VALUE - 6), stringsField);
    }

    private SQLJsonIEntityValueBuilder builder;

    @Before
    public void before() throws Exception {
        builder = new SQLJsonIEntityValueBuilder();

        ReflectionTestUtils.setField(builder, "storageStrategyFactory", storageStrategyFactory);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: build(long id, Map<String, IEntityField> fieldTable, String json)
     */
    @Test
    public void testBuild() throws Exception {
        buildCases().stream().forEach(c -> {
            IEntityValue value;
            try {
                value = builder.build(c.id, FIELDS_TABLE, c.json);

            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            Assert.assertEquals(c.id, value.id());
            Assert.assertEquals(c.expectedValue, value.getValue(c.id).get());
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                Long.MAX_VALUE,
                String.format("{\"%dL\":300}", Long.MAX_VALUE),
                new LongValue(longField, 300)
            )
            ,
            new Case(
                Long.MAX_VALUE - 4,
                String.format("{\"%dS\":\"0.0\"}", Long.MAX_VALUE - 4),
                new DecimalValue(decimalField, new BigDecimal("0.0"))
            )
            ,
            new Case(
                Long.MAX_VALUE - 4,
                String.format("{\"%dS\":\"0\"}", Long.MAX_VALUE - 4),
                new DecimalValue(decimalField, new BigDecimal("0"))
            )
            ,
            new Case(
                Long.MAX_VALUE - 1,
                String.format("{\"%dS\":\"test\"}", Long.MAX_VALUE - 1),
                new StringValue(stringField, "test")
            )
        );
    }

    private static class Case {
        private long id;
        private String json;
        private IValue expectedValue;

        public Case(long id, String json, IValue expectedValue) {
            this.id = id;
            this.json = json;
            this.expectedValue = expectedValue;
        }
    }
} 
