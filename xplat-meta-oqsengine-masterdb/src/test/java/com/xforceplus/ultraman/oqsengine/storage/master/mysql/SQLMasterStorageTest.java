package com.xforceplus.ultraman.oqsengine.storage.master.mysql;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgentFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * SQLMasterStorage Tester.
 *
 * @author dongbin
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class})
public class SQLMasterStorageTest {

    private TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
    private SQLMasterStorage storage = MasterDBInitialization.getInstance().getMasterStorage();

    /*
    静态对象测试.
     */
    private static final String ORGINAL_TAG = "original";
    /*
    动态对象测试.
     */
    private static final String DYNAMIC_TAG = "dynamic";

    //-------------level 0--------------------
    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(1000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(1001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();
    private EntityClassRef l0EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l0EntityClass.id()).withEntityClassCode(l0EntityClass.code())
            .build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("l1-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("l1-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
        .build();
    private EntityClassRef l1EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l1EntityClass.id()).withEntityClassCode(l1EntityClass.code())
            .build();

    //-------------level 2--------------------
    private IEntityField l2LongField = EntityField.Builder.anEntityField()
        .withId(3000)
        .withFieldType(FieldType.LONG)
        .withName("l2-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(3001)
        .withFieldType(FieldType.STRING)
        .withName("l2-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(3)
        .withLevel(2)
        .withCode("l2")
        .withField(l2LongField)
        .withField(l2StringField)
        .withFather(l1EntityClass)
        .build();
    private EntityClassRef l2EntityClassRef =
        EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(l2EntityClass.id()).withEntityClassCode(l2EntityClass.code())
            .build();

    //-------------original-------------------
    private IEntityField originalLongField = EntityField.Builder.anEntityField()
        .withId(4000)
        .withFieldType(FieldType.LONG)
        .withName("original_long")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.BIGINT).withSearchable(true).build())
        .build();
    private IEntityField originalStringField = EntityField.Builder.anEntityField()
        .withId(4001)
        .withFieldType(FieldType.STRING)
        .withName("original_string")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.VARCHAR).withSearchable(true).build())
        .build();
    private IEntityField originalStringsField = EntityField.Builder.anEntityField()
        .withId(4003)
        .withFieldType(FieldType.STRINGS)
        .withName("original_strings")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.VARCHAR).withSearchable(true).build())
        .build();
    private IEntityField originalDecField = EntityField.Builder.anEntityField()
        .withId(4004)
        .withFieldType(FieldType.DECIMAL)
        .withName("original_dec")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.DECIMAL).withSearchable(true).build())
        .build();
    private IEntityField originalEnumField = EntityField.Builder.anEntityField()
        .withId(4005)
        .withFieldType(FieldType.ENUM)
        .withName("original_enum")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.VARCHAR).withSearchable(true).build())
        .build();
    private IEntityField originalDatetimeField = EntityField.Builder.anEntityField()
        .withId(4006)
        .withFieldType(FieldType.DATETIME)
        .withName("original_datetime")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.BIGINT).withSearchable(true).build())
        .build();
    private IEntityField originaBoolField = EntityField.Builder.anEntityField()
        .withId(4007)
        .withFieldType(FieldType.BOOLEAN)
        .withName("original_bool")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.BOOLEAN).withSearchable(true).build())
        .build();
    private IEntityClass originalEntityClass = EntityClass.Builder.anEntityClass()
        .withId(4)
        .withLevel(0)
        .withAppCode("test")
        .withCode("original_table")
        .withType(EntityClassType.ORIGINAL)
        .withName("original_table")
        .withField(originalLongField)
        .withField(originalStringField)
        .withField(originalStringsField)
        .withField(originalDecField)
        .withField(originalEnumField)
        .withField(originalDatetimeField)
        .withField(originaBoolField)
        .build();

    private List<IEntity> expectedEntitys;

    public SQLMasterStorageTest() throws Exception {
    }

    /**
     * 每个测试初始化.
     */
    @BeforeEach
    public void before(TestInfo testInfo) throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(l2EntityClass, originalEntityClass));

        Set<String> tags = testInfo.getTags();
        for (String tag : tags) {
            // 初始化动态测试数据.
            if (DYNAMIC_TAG.equals(tag)) {
                expectedEntitys = initData(storage, 100);
            }
        }
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    @Tag(ORGINAL_TAG)
    public void testOriginalDeleteEntity() throws Exception {

    }

    @Test
    @Tag(ORGINAL_TAG)
    public void testOriginalReplaceEntity() throws Exception {
        LocalDateTime time = LocalDateTime.now();
        IEntity newEntity = Entity.Builder.anEntity()
            .withId(100000)
            .withEntityClassRef(originalEntityClass.ref())
            .build();
        newEntity.entityValue().addValues(
            Arrays.asList(
                new LongValue(originalLongField, 100),
                new StringValue(originalStringField, "original"),
                new StringsValue(originalStringsField, "v1", "v2", "v3"),
                new DecimalValue(originalDecField, new BigDecimal("100.123")),
                new EnumValue(originalEnumField, "1"),
                new DateTimeValue(originalDatetimeField, time),
                new BooleanValue(originaBoolField, true)
            )
        );
        Assertions.assertTrue(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为脏的.
        Assertions.assertEquals(newEntity.entityValue().size(),
            newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());
        Assertions.assertTrue(storage.build(newEntity, originalEntityClass));
        Assertions.assertFalse(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为干净的.
        Assertions.assertEquals(0, newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());

        // 增加一个干扰数据.
        IEntity interferenceEntity = Entity.Builder.anEntity()
            .withId(200000)
            .withEntityClassRef(originalEntityClass.ref())
            .build();
        interferenceEntity.entityValue().addValues(
            Arrays.asList(
                new LongValue(originalLongField, 100),
                new StringValue(originalStringField, "original"),
                new StringsValue(originalStringsField, "v1", "v2", "v3"),
                new DecimalValue(originalDecField, new BigDecimal("100.123")),
                new EnumValue(originalEnumField, "1"),
                new DateTimeValue(originalDatetimeField, time),
                new BooleanValue(originaBoolField, true)
            )
        );
        Assertions.assertTrue(storage.build(interferenceEntity, originalEntityClass));

        // 更新目标实例浮点数.
        IEntity updateEntity = Entity.Builder.anEntity()
            .withId(newEntity.id())
            .withEntityClassRef(originalEntityClass.ref())
            .withValue(new DecimalValue(originalDecField, new BigDecimal("200.123")))
            .withValue(new StringValue(originalStringField, "original-change"))
            .build();
        Assertions.assertTrue(storage.replace(updateEntity, originalEntityClass));
        updateEntity = storage.selectOne(updateEntity.id()).get();

        IEntityValue entityValue = updateEntity.entityValue();
        Assertions.assertEquals(100, entityValue.getValue(originalLongField.id()).get().valueToLong());
        Assertions.assertEquals("original-change",
            entityValue.getValue(originalStringField.id()).get().valueToString());
        Assertions.assertArrayEquals(new String[] {
            "v1", "v2", "v3"
        }, (String[]) entityValue.getValue(originalStringsField.id()).get().getValue());
        Assertions.assertEquals(new BigDecimal("200.123"),
            entityValue.getValue(originalDecField.id()).get().getValue());
        Assertions.assertEquals("1", entityValue.getValue(originalEnumField.id()).get().valueToString());

        long timeMilli = time.atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli();

        Assertions.assertEquals(timeMilli,
            ((LocalDateTime) entityValue.getValue(originalDatetimeField.id()).get().getValue())
                .atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli());
        Assertions.assertTrue((Boolean) entityValue.getValue(originaBoolField.id()).get().getValue());

        List<Map<IEntityField, StorageValue>> rows = findOriginalRow(originalEntityClass, new long[] { newEntity.id() });
        Assertions.assertEquals(1, rows.size());

        Map<IEntityField, StorageValue> row = rows.get(0);
        Collection<IEntityField> fields = originalEntityClass.fields();
        Assertions.assertEquals(fields.size(), row.size());
        Assertions.assertEquals(100L, row.get(originalLongField).value());
        Assertions.assertEquals("original-change", row.get(originalStringField).value());
        Assertions.assertEquals("[v1][v2][v3]", row.get(originalStringsField).value());

        Assertions.assertEquals(200L, row.get(originalDecField).value());
        Assertions.assertEquals(123000L, row.get(originalDecField).next().value());
        Assertions.assertEquals("1", row.get(originalEnumField).value());
        Assertions.assertEquals(1L, row.get(originaBoolField).value());
        Assertions.assertEquals(timeMilli, row.get(originalDatetimeField).value());

        // 验证干扰数据不变
        interferenceEntity = storage.selectOne(interferenceEntity.id()).get();
        entityValue = interferenceEntity.entityValue();
        Assertions.assertEquals(100, entityValue.getValue(originalLongField.id()).get().valueToLong());
        Assertions.assertEquals("original",
            entityValue.getValue(originalStringField.id()).get().valueToString());
        Assertions.assertArrayEquals(new String[] {
            "v1", "v2", "v3"
        }, (String[]) entityValue.getValue(originalStringsField.id()).get().getValue());
        Assertions.assertEquals(new BigDecimal("100.123"),
            entityValue.getValue(originalDecField.id()).get().getValue());
        Assertions.assertEquals("1", entityValue.getValue(originalEnumField.id()).get().valueToString());
        Assertions.assertEquals(timeMilli,
            ((LocalDateTime) entityValue.getValue(originalDatetimeField.id()).get().getValue())
                .atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli());
        Assertions.assertTrue((Boolean) entityValue.getValue(originaBoolField.id()).get().getValue());

        // 验证干扰实例的静态数据
        rows = findOriginalRow(originalEntityClass, new long[] { interferenceEntity.id() });
        Assertions.assertEquals(1, rows.size());

        row = rows.get(0);
        fields = originalEntityClass.fields();
        Assertions.assertEquals(fields.size(), row.size());
        Assertions.assertEquals(100L, row.get(originalLongField).value());
        Assertions.assertEquals("original", row.get(originalStringField).value());
        Assertions.assertEquals("[v1][v2][v3]", row.get(originalStringsField).value());

        Assertions.assertEquals(100L, row.get(originalDecField).value());
        Assertions.assertEquals(123000L, row.get(originalDecField).next().value());
        Assertions.assertEquals("1", row.get(originalEnumField).value());
        Assertions.assertEquals(1L, row.get(originaBoolField).value());
        Assertions.assertEquals(timeMilli, row.get(originalDatetimeField).value());
    }

    @Test
    @Tag(ORGINAL_TAG)
    public void testOriginalBuildEntity() throws Exception {
        LocalDateTime time = LocalDateTime.now();
        IEntity newEntity = Entity.Builder.anEntity()
            .withId(100000)
            .withEntityClassRef(originalEntityClass.ref())
            .build();
        newEntity.entityValue().addValues(
            Arrays.asList(
                new LongValue(originalLongField, 100),
                new StringValue(originalStringField, "original"),
                new StringsValue(originalStringsField, "v1", "v2", "v3"),
                new DecimalValue(originalDecField, new BigDecimal("100.123")),
                new EnumValue(originalEnumField, "1"),
                new DateTimeValue(originalDatetimeField, time),
                new BooleanValue(originaBoolField, true)
            )
        );
        Assertions.assertTrue(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为脏的.
        Assertions.assertEquals(newEntity.entityValue().size(),
            newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());
        boolean result = storage.build(newEntity, originalEntityClass);
        Assertions.assertTrue(result);
        Assertions.assertFalse(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为干净的.
        Assertions.assertEquals(0, newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());

        Optional<IEntity> entityOptional = storage.selectOne(newEntity.id(), originalEntityClass);
        Assertions.assertTrue(entityOptional.isPresent());
        IEntity targetEntity = entityOptional.get();
        IEntityValue entityValue = targetEntity.entityValue();
        Assertions.assertEquals(100, entityValue.getValue(originalLongField.id()).get().valueToLong());
        Assertions.assertEquals("original",
            entityValue.getValue(originalStringField.id()).get().valueToString());
        Assertions.assertArrayEquals(new String[] {
            "v1", "v2", "v3"
        }, (String[]) entityValue.getValue(originalStringsField.id()).get().getValue());
        Assertions.assertEquals(new BigDecimal("100.123"),
            entityValue.getValue(originalDecField.id()).get().getValue());
        Assertions.assertEquals("1", entityValue.getValue(originalEnumField.id()).get().valueToString());

        long timeMilli = time.atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli();

        Assertions.assertEquals(timeMilli,
            ((LocalDateTime) entityValue.getValue(originalDatetimeField.id()).get().getValue())
                .atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli());
        Assertions.assertTrue((Boolean) entityValue.getValue(originaBoolField.id()).get().getValue());

        List<Map<IEntityField, StorageValue>> rows = findOriginalRow(originalEntityClass, new long[] { newEntity.id() });
        Assertions.assertEquals(1, rows.size());

        Map<IEntityField, StorageValue> row = rows.get(0);
        Collection<IEntityField> fields = originalEntityClass.fields();
        Assertions.assertEquals(fields.size(), row.size());
        Assertions.assertEquals(100L, row.get(originalLongField).value());
        Assertions.assertEquals("original", row.get(originalStringField).value());
        Assertions.assertEquals("[v1][v2][v3]", row.get(originalStringsField).value());

        Assertions.assertEquals(100L, row.get(originalDecField).value());
        Assertions.assertEquals(123000L, row.get(originalDecField).next().value());
        Assertions.assertEquals("1", row.get(originalEnumField).value());
        Assertions.assertEquals(1L, row.get(originaBoolField).value());
        Assertions.assertEquals(timeMilli, row.get(originalDatetimeField).value());
    }

    /**
     * 测试写入并查询.
     */
    @Test
    @Tag(DYNAMIC_TAG)
    public void testBuildEntity() throws Exception {

        LocalDateTime updateTime = LocalDateTime.now();
        IEntity newEntity = Entity.Builder.anEntity()
            .withId(100000)
            .withEntityClassRef(l1EntityClassRef)
            .build();
        newEntity.entityValue().addValues(
            Arrays.asList(
                new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100),
                new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                new LongValue(l1EntityClass.field("l1-long").get(), 200),
                new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                new DateTimeValue(EntityField.UPDATE_TIME_FILED, updateTime)
            )
        );
        Assertions.assertTrue(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为脏的.
        Assertions.assertEquals(newEntity.entityValue().size(),
            newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());
        boolean result = storage.build(newEntity, l1EntityClass);
        Assertions.assertTrue(result);
        Assertions.assertFalse(newEntity.isDirty());
        Assertions.assertFalse(newEntity.isDeleted());
        // 所有值都应该为干净的.
        Assertions.assertEquals(0, newEntity.entityValue().values().stream().filter(v -> v.isDirty()).count());

        Optional<IEntity> entityOptional = storage.selectOne(newEntity.id(), l1EntityClass);
        Assertions.assertTrue(entityOptional.isPresent());
        IEntity targetEntity = entityOptional.get();
        Assertions.assertEquals(100, targetEntity.entityValue().getValue("l0-long").get().valueToLong());
        Assertions.assertEquals("l0value", targetEntity.entityValue().getValue("l0-string").get().valueToString());
        Assertions.assertEquals(200, targetEntity.entityValue().getValue("l1-long").get().valueToLong());
        Assertions.assertEquals("l1value", targetEntity.entityValue().getValue("l1-string").get().valueToString());
        Assertions.assertEquals(0, targetEntity.version());
        Assertions.assertEquals(updateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
            entityOptional.get().time());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testSelectOne() throws Exception {
        List<IEntity> entities = new ArrayList<>(expectedEntitys.size());
        expectedEntitys.stream().mapToLong(e -> e.id()).forEach(id -> {
            Optional<IEntity> entityOp;
            try {
                entityOp = storage.selectOne(id, l1EntityClass);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            entities.add(entityOp.get());
        });

        Assertions.assertEquals(expectedEntitys.size(), entities.size());
        for (int i = 0; i < expectedEntitys.size(); i++) {
            IEntity expectedEntity = expectedEntitys.get(i);
            IEntity targetEntity = entities.get(i);

            Collection<IValue> expectValues = expectedEntity.entityValue().values();
            Assertions.assertFalse(targetEntity.isDirty());
            Assertions.assertFalse(targetEntity.isDeleted());
            Assertions.assertEquals(expectValues.size(), targetEntity.entityValue().size());

            for (IValue expectedValue : expectValues) {
                IValue targetValue = targetEntity.entityValue().getValue(expectedValue.getField().id()).get();

                Assertions.assertTrue(expectedValue.equals(targetValue));
            }
        }
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testBuildEntities() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        int expectedSize = 1000;
        for (int i = 0; i < expectedSize; i++) {
            IEntity entity = Entity.Builder.anEntity()
                .withId(100000 + i)
                .withEntityClassRef(l1EntityClassRef)
                .build();
            entity.entityValue().addValues(
                Arrays.asList(
                    new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100 + i),
                    new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                    new LongValue(l1EntityClass.field("l1-long").get(), 200 + i),
                    new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                    new DateTimeValue(EntityField.UPDATE_TIME_FILED, LocalDateTime.now())
                )
            );
            entityPackage.put(entity, l1EntityClass);
        }

        Assertions.assertEquals(expectedSize, entityPackage.stream().filter(en -> en.getKey().isDirty()).count());

        storage.build(entityPackage);

        Assertions.assertEquals(expectedSize,
            entityPackage.stream().filter(en -> !en.getKey().isDirty()).count());

        long[] ids = IntStream.range(0, expectedSize).mapToLong(i -> 100000 + i).toArray();
        List<IEntity> entities = new ArrayList(storage.selectMultiple(ids));
        Collections.sort(entities, (o1, o2) -> {
            if (o1.id() < o2.id()) {
                return -1;
            } else if (o1.id() > o2.id()) {
                return 1;
            } else {
                return 0;
            }
        });

        for (int i = 0; i < expectedSize; i++) {
            IEntity entity = entities.get(i);
            Assertions.assertEquals(100 + i, entity.entityValue().getValue("l0-long").get().valueToLong());
            Assertions.assertEquals("l0value", entity.entityValue().getValue("l0-string").get().valueToString());
            Assertions.assertEquals(200 + i, entity.entityValue().getValue("l1-long").get().valueToLong());
            Assertions.assertEquals("l1value", entity.entityValue().getValue("l1-string").get().valueToString());
            Assertions.assertEquals(0, entity.version());
        }

    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testSelectMultiple() throws Exception {
        long[] ids = expectedEntitys.stream().mapToLong(e -> e.id()).toArray();
        Collection<IEntity> entities = storage.selectMultiple(ids, l1EntityClass);

        Map<Long, com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> expectedEntityMap =
            expectedEntitys.stream().collect(Collectors.toMap(e -> e.id(), e -> e, (e0, e1) -> e0));

        Assertions.assertEquals(expectedEntityMap.size(), entities.size());

        IEntity expectedEntity;
        for (IEntity e : entities) {
            expectedEntity = expectedEntityMap.get(e.id());
            Assertions.assertNotNull(
                expectedEntity, String.format("An instance of the %d object should be found, but not found.", e.id()));

            Assertions.assertEquals(expectedEntity, e);
            // 实际类型是l2EntityClass.
            Assertions.assertEquals(l2EntityClassRef, e.entityClassRef());
        }
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testReplace() throws Exception {
        LocalDateTime updateTime = LocalDateTime.now();
        IEntity targetEntity = expectedEntitys.get(0);
        targetEntity.entityValue().addValue(
            new LongValue(
                l1EntityClass.father().get().field("l0-long").get(), 1000000, "new-attachement")
        ).addValue(
            new DateTimeValue(EntityField.UPDATE_TIME_FILED, updateTime)
        ).addValue(
            new EmptyTypedValue(l2StringField)
        );

        Assertions.assertEquals(0, targetEntity.version());
        Assertions.assertTrue(targetEntity.isDirty());

        int oldVersion = targetEntity.version();


        boolean result = storage.replace(targetEntity, l2EntityClass);
        Assertions.assertTrue(result);
        Assertions.assertFalse(targetEntity.isDirty());


        Optional<IEntity> targetEntityOp = storage.selectOne(targetEntity.id(), l2EntityClass);
        Assertions.assertTrue(targetEntityOp.isPresent());
        Assertions.assertEquals(1000000L,
            targetEntityOp.get().entityValue().getValue("l0-long").get().valueToLong());
        Assertions.assertEquals(oldVersion + 1, targetEntityOp.get().version());
        Assertions.assertEquals(updateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
            targetEntityOp.get().time());
        Assertions.assertEquals("new-attachement",
            targetEntityOp.get().entityValue().getValue("l0-long").get().getAttachment().get());
        Assertions.assertFalse(targetEntityOp.get().entityValue().getValue("l2-string").isPresent());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testReplaceEntities() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        int expectedDirtySize = 1000;
        for (int i = 0; i < expectedDirtySize; i++) {
            IEntity entity = Entity.Builder.anEntity()
                .withId(100000 + i)
                .withEntityClassRef(l1EntityClassRef)
                .build();
            entity.entityValue().addValues(
                Arrays.asList(
                    new LongValue(l1EntityClass.father().get().field("l0-long").get(), 100 + i),
                    new StringValue(l1EntityClass.father().get().field("l0-string").get(), "l0value"),
                    new LongValue(l1EntityClass.field("l1-long").get(), 200 + i),
                    new StringValue(l1EntityClass.field("l1-string").get(), "l1value"),
                    new DateTimeValue(EntityField.UPDATE_TIME_FILED, LocalDateTime.now())
                )
            );
            entityPackage.put(entity, l1EntityClass);
        }

        Assertions.assertEquals(expectedDirtySize, entityPackage.stream().filter(en -> en.getKey().isDirty()).count());
        storage.build(entityPackage);
        Assertions.assertEquals(expectedDirtySize,
            entityPackage.stream().filter(en -> !en.getKey().isDirty()).count());

        EntityPackage updatePackage = new EntityPackage();
        entityPackage.stream().map(e -> {
            e.getKey().entityValue()
                .addValue(new LongValue(l1EntityClass.father().get().field("l0-long").get(), -100))
                .addValue(new EmptyTypedValue(l1EntityClass.field("l1-string").get()));
            return e;
        }).forEach(e -> {
            updatePackage.put(e.getKey(), e.getValue());
        });

        // 加入10条干净,不应该被更新.
        int expectedCleanSize = 10;
        for (int i = 0; i < expectedCleanSize; i++) {
            updatePackage.put(this.expectedEntitys.get(i), l2EntityClass);
        }

        Assertions.assertEquals(expectedDirtySize, updatePackage.stream().filter(en -> en.getKey().isDirty()).count());
        storage.replace(updatePackage);
        Assertions.assertEquals(expectedDirtySize + expectedCleanSize,
            updatePackage.stream().filter(en -> !en.getKey().isDirty()).count());

        // 得到应该被更新的实例id列表
        long[] ids = entityPackage.stream().mapToLong(e -> e.getKey().id()).toArray();
        Collection<IEntity> entities = storage.selectMultiple(ids);
        Assertions.assertEquals(expectedDirtySize,
            entities.stream()
                .filter(e -> e.entityValue().getValue("l0-long").get().valueToLong() == -100).count());

        Assertions.assertEquals(0,
            entities.stream()
                .filter(e -> e.entityValue().getValue("l1-string").isPresent()).count());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testReplaceJson() throws Exception {
        IEntity targetEntity = expectedEntitys.get(1);
        targetEntity.entityValue().addValue(
            new StringValue(l2EntityClass.field("l2-string").get(),
                "[{\n   \"c1\":\"c1-value\", \"c2\": 123, \"c3\": \"test'value\"},]"
            ));
        boolean result = storage.replace(targetEntity, l2EntityClass);
        Assertions.assertTrue(result);

        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        Assertions.assertEquals("[{\n   \"c1\":\"c1-value\", \"c2\": 123, \"c3\": \"test'value\"},]",
            targetEntity.entityValue().getValue("l2-string").get().valueToString());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testBuildJson() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(1000000)
            .withEntityClassRef(l2EntityClassRef).build();
        targetEntity.entityValue()
            .addValue(
                new StringValue(l2EntityClass.field("l2-string").get(),
                    "[{\n   \"c1\":\"c1-value\", \"c2\": 123, \"c3\": \"test'value\"},]"
                )
            );

        boolean result = storage.build(targetEntity, l2EntityClass);
        Assertions.assertTrue(result);

        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        Assertions.assertEquals("[{\n   \"c1\":\"c1-value\", \"c2\": 123, \"c3\": \"test'value\"},]",
            targetEntity.entityValue().getValue("l2-string").get().valueToString());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testDelete() throws Exception {
        IEntity targetEntity = expectedEntitys.get(1);
        storage.replace(targetEntity, l2EntityClass);
        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();

        Assertions.assertTrue(storage.delete(targetEntity, l2EntityClass));
        Assertions.assertTrue(targetEntity.isDeleted());
        Assertions.assertFalse(targetEntity.isDirty());

        Assertions.assertFalse(storage.selectOne(targetEntity.id(), l2EntityClass).isPresent());

        Assertions.assertFalse(storage.exist(targetEntity.id()) >= 0);
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testDeletes() throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        expectedEntitys.stream().forEach(e -> {
            entityPackage.put(e, l2EntityClass);
        });

        storage.delete(entityPackage);
        Assertions.assertEquals(expectedEntitys.size(),
            expectedEntitys.stream().filter(e -> e.isDeleted()).count());
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testDeleteWithoutVersion() throws Exception {
        IEntity targetEntity = expectedEntitys.get(2);
        storage.replace(targetEntity, l2EntityClass);
        targetEntity = storage.selectOne(targetEntity.id(), l2EntityClass).get();
        targetEntity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);

        Assertions.assertTrue(storage.delete(targetEntity, l2EntityClass));
        Assertions.assertFalse(storage.selectOne(targetEntity.id(), l2EntityClass).isPresent());
        Assertions.assertFalse(storage.exist(targetEntity.id()) >= 0);
    }

    @Test
    @Tag(DYNAMIC_TAG)
    public void testExist() throws Exception {
        IEntity targetEntity = expectedEntitys.get(2);

        Assertions.assertEquals(0, storage.exist(targetEntity.id()));

        Assertions.assertFalse(storage.exist(-1) >= 0);
    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        EntityPackage entityPackage = new EntityPackage();
        for (int i = 1; i <= size; i++) {
            IEntity entity = buildEntity(i * size);
            expectedEntitys.add(entity);
            entityPackage.put(entity, l2EntityClass);
        }

        try {
            storage.build(entityPackage);
            StorageInitialization.getInstance().getCommitIdStatusService().obsoleteAll();
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        tx = transactionManager.getCurrent().get();

        // 表示为非可读事务.
        for (IEntity e : expectedEntitys) {
            tx.getAccumulator().accumulateBuild(e);
        }

        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        IEntity entity = Entity.Builder.anEntity()
            .withId(baseId)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(l2EntityClassRef)
            .withVersion(0)
            .build();
        entity.entityValue().addValues(
            buildValue(baseId,
                Arrays.asList(
                    l0LongField, l0StringField, l0StringsField,
                    l1LongField, l1StringField,
                    l2LongField, l2StringField))
        );
        return entity;
    }

    private Collection<IValue> buildValue(long id, Collection<IEntityField> fields) {
        return fields.stream().map(f -> {
            switch (f.type()) {
                case STRING: {
                    String randomString = buildRandomString(10);
                    return new StringValue(f, randomString, randomString);
                }
                case STRINGS: {
                    String randomString0 = buildRandomString(5);
                    String randomString1 = buildRandomString(3);
                    String randomString2 = buildRandomString(7);
                    return new StringsValue(f, new String[] {randomString0, randomString1, randomString2},
                        randomString0);
                }
                default: {
                    long randomLong = buildRandomLong(10, 100000);
                    return new LongValue(f, randomLong, Long.toString(randomLong));
                }
            }
        }).collect(Collectors.toList());
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    // 查看原始表中的数据.
    private List<Map<IEntityField, StorageValue>> findOriginalRow(IEntityClass entityClass, long[] ids) throws Exception {
        DataSource ds = MasterDBInitialization.getInstance().getDataSource();
        Connection conn = ds.getConnection();
        String sql = String.format(
            "SELECT %s FROM %s WHERE id IN (%s)",
            entityClass.fields().stream().map(f -> f.name()).collect(Collectors.joining(", ")),
            String.format("%s_%s", entityClass.appCode(), entityClass.code()),
            IntStream.range(0, ids.length).mapToObj(i -> "?").collect(Collectors.joining(", ")));
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setLong(i + 1, ids[i]);
            }

            List<Map<IEntityField, StorageValue>> rows = new ArrayList<>();
            Map<IEntityField, StorageValue> row;
            JdbcOriginalFieldAgentFactory agentFactory = JdbcOriginalFieldAgentFactory.getInstance();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    row = new HashMap<>(MapUtils.calculateInitSize(entityClass.fields().size()));
                    for (IEntityField field : entityClass.fields()) {
                        JdbcOriginalFieldAgent agent =
                            (JdbcOriginalFieldAgent) agentFactory.getAgent(field.config().getJdbcType());
                        int colIndex = -1;
                        try {
                            colIndex = rs.findColumn(field.name());
                        } catch (Exception ex) {
                            colIndex = -1;
                        }
                        if (colIndex < 0) {
                            row.put(field, null);
                        } else {
                            row.put(field, agent.read(field, new ReadJdbcOriginalSource(colIndex, rs)));
                        }
                    }

                    rows.add(row);
                }
            }

            return rows;
        }
    }
}
