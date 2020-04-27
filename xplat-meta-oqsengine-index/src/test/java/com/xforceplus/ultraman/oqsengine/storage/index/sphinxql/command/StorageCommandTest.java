package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/22/2020 10:55 AM
 * 功能描述:
 * 修改历史:
 */
public class StorageCommandTest {

    private DataSourcePackage dataSourcePackage;

    private StorageStrategyFactory storageStrategyFactory;

    private static IEntityField longField = new EntityField(Long.MAX_VALUE, "long", FieldType.LONG);
    private static IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING);
    private static IEntityField boolField = new EntityField(Long.MAX_VALUE - 2, "bool", FieldType.BOOLEAN);
    private static IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 3, "datetime", FieldType.DATETIME);
    private static IEntityField decimalField = new EntityField(Long.MAX_VALUE - 4, "decimal", FieldType.DECIMAL);
    private static IEntityField enumField = new EntityField(Long.MAX_VALUE - 5, "enum", FieldType.ENUM);
    private static IEntityField stringsField = new EntityField(Long.MAX_VALUE - 6, "strings", FieldType.STRINGS);

    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "test", Arrays.asList(
            longField,
            stringField,
            boolField,
            dateTimeField,
            decimalField,
            enumField,
            stringsField
    ));

    private StorageEntity storageEntity;

    private TransactionResource resource;

    @Before
    public void init() {
        buildWriteDataSourceSelector("./src/test/resources/sql_index_storage.conf");
        buildSearchDataSourceSelector("./src/test/resources/sql_index_storage.conf");

        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        long id = Long.MAX_VALUE;
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(
                new LongValue(longField, 1L),
                new StringValue(stringField, "v1"),
                new BooleanValue(boolField, true),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ZERO),
                new EnumValue(enumField, "1"),
                new StringsValue(stringsField, "value1", "value2")
        ));
        IEntity entity = new Entity(id, entityClass, values);

        storageEntity = new StorageEntity(
                entity.id(),
                entity.entityClass().id(),
                entity.family().parent(),
                entity.family().child(),
                serializeToMap(entity.entityValue(), true),
                serializeSetFull(entity.entityValue())
        );

        List<DataSource> ds = dataSourcePackage.getIndexWriter();
        try {
            resource = new SphinxQLTransactionResource("1", ds.get(0).getConnection(), true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void storageCommandTest() {
        List<DataSource> ds = dataSourcePackage.getIndexWriter();

        //test DeleteStorageCommand
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                new DeleteStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        //test BuildStorageCommandTest
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                new BuildStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                StorageEntity res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
                //verify
                Assert.assertTrue(res != null);
                Assert.assertTrue(res.getId() == Long.MAX_VALUE);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        //test ReplaceStorageCommand
        storageEntity.setEntity(Long.MAX_VALUE - 1);
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                new ReplaceStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                StorageEntity res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
                //verify
                Assert.assertTrue(res != null);
                Assert.assertTrue(res.getEntity() == (Long.MAX_VALUE - 1));
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        //test DeleteStorageCommand
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                new DeleteStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            StorageEntity res = null;
            try {
                TransactionResource resource = new SphinxQLTransactionResource("1", dataSource.getConnection(), true);
                res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            Assert.assertTrue(res == null);
        });
    }

    @Test
    public void storageCommandUndoTest() {
        try {
            //prepare
            new DeleteStorageCommand("oqsindextest").execute(resource, storageEntity);

            // test BuildStorageCommand
            //do
            new BuildStorageCommand("oqsindextest").execute(resource, storageEntity);
            StorageEntity res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getId() == Long.MAX_VALUE);
            //undo
            new BuildStorageCommand("oqsindextest").executeUndo(resource, storageEntity);

            res = null;
            try {
                res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException e) {

            }
            //verify
            Assert.assertTrue(res == null);

            // test DeleteStorageCommand
            //prepare
            new BuildStorageCommand("oqsindextest").execute(resource, storageEntity);
            //do
            new DeleteStorageCommand("oqsindextest").execute(resource, storageEntity);
            res = null;
            try {
                res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            } catch (SQLException e) {

            }
            //verify
            Assert.assertTrue(res == null);
            //undo
            new DeleteStorageCommand("oqsindextest").executeUndo(resource, storageEntity);
            res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getId() == Long.MAX_VALUE);

            // test ReplaceStorageCommand
            // do
            long oriEntity = storageEntity.getEntity();
            storageEntity.setEntity(123456);
            new ReplaceStorageCommand("oqsindextest").execute(resource, storageEntity);
            res = new SelectByIdStorageCommand("oqsindextest").execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getEntity() == 123456);
            // undo
            storageEntity.setEntity(oriEntity);
            new ReplaceStorageCommand("oqsindextest").executeUndo(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getEntity() == oriEntity);
        } catch (SQLException e) {

        }

    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);
            dataSourcePackage = DataSourceFactory.build();
        }
        return new TakeTurnsSelector<>(dataSourcePackage.getIndexWriter());
    }

    private Selector<DataSource> buildSearchDataSourceSelector(String file) {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);
            dataSourcePackage = DataSourceFactory.build();
        }
        return new TakeTurnsSelector<>(dataSourcePackage.getIndexSearch());
    }

    /**
     * <f>fieldId + fieldvalue(unicode)</f> + <f>fieldId + fieldvalue(unicode)</f>....n
     */
    private Set<String> serializeSetFull(IEntityValue entityValue) {
        Set<String> fullSet = new HashSet<>();
        entityValue.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);
            while (storageValue != null) {
                // 这里已经处理成了<F3S>F3S test</F3S>的储存样式.
                fullSet.add(serializeStorageValueFull(storageValue));
                storageValue = storageValue.next();
            }
        });
        return fullSet;
    }

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private Map<String, Object> serializeToMap(IEntityValue values, boolean encodeString) {
        Map<String, Object> data = new HashMap<>(values.values().size());
        values.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);
            while (storageValue != null) {
                if (storageValue.type() == StorageType.STRING) {
                    data.put(storageValue.storageName(),
                            encodeString ? SphinxQLHelper.encodeSpecialCharset((String) storageValue.value()) : storageValue.value());
                } else {
                    data.put(storageValue.storageName(), storageValue.value());
                }
                storageValue = storageValue.next();
            }
        });

        return data;
    }

    // 处理成<F123L>F123L 789</F123L> 形式字符串.
    private String serializeStorageValueFull(StorageValue value) {
        StringBuilder buff = new StringBuilder();
        buff.append("<").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        buff.append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.storageName()).append(' ');
        if (value.type() == StorageType.STRING) {
            buff.append(SphinxQLHelper.encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append("</").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        return buff.toString();
    }
}

