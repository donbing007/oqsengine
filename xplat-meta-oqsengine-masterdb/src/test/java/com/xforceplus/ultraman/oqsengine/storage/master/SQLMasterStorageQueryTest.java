package com.xforceplus.ultraman.oqsengine.storage.master;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 主库搜索测试.
 *
 * @author dongbin
 * @version 0.1 2020/11/6 16:16
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class})
public class SQLMasterStorageQueryTest {

    private TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
    private SQLMasterStorage storage = MasterDBInitialization.getInstance().getMasterStorage();

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
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1002)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0EnumField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.ENUM)
        .withName("l0-enum")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DecimalField = EntityField.Builder.anEntityField()
        .withId(1004)
        .withFieldType(FieldType.DECIMAL)
        .withName("l0-decimal")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DatetimeField = EntityField.Builder.anEntityField()
        .withId(1005)
        .withFieldType(FieldType.DATETIME)
        .withName("l0-datetime")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .withField(l0EnumField)
        .withField(l0DecimalField)
        .withField(l0DatetimeField)
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
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
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
    private IEntityField l2bigintField = EntityField.Builder.anEntityField()
        .withId(3002)
        .withFieldType(FieldType.LONG)
        .withName("l2-bigint")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringSegmentationField = EntityField.Builder.anEntityField()
        .withId(3003)
        .withFieldType(FieldType.STRING)
        .withName("l2-string-segmentation")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION).withSearchable(true).build()).build();
    private IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(3)
        .withLevel(2)
        .withCode("l2")
        .withField(l2LongField)
        .withField(l2StringField)
        .withField(l2bigintField)
        .withField(l2StringSegmentationField)
        .withFather(l1EntityClass)
        .build();

    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entityes;

    public SQLMasterStorageQueryTest() throws Exception {
    }

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(l2EntityClass));

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        try {
            initData(storage);

            // 表示为只读事务.
            for (IEntity e : entityes) {
                tx.getAccumulator().accumulateBuild(e);
            }
            tx.commit();
        } catch (Exception ex) {

            if (!tx.isCompleted()) {
                tx.rollback();
            }

            throw ex;

        } finally {
            transactionManager.finish();
        }

        // 确认没有事务.
        Assertions.assertFalse(transactionManager.getCurrent().isPresent());
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void testActualEntityClass() throws Exception {
        Optional<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entityOp = storage.selectOne(entityes.get(0).id(), l0EntityClass);
        Assertions.assertTrue(entityOp.isPresent());
        IEntity entity = entityOp.get();
        Assertions.assertEquals(l2EntityClass.ref(), entity.entityClassRef());

        Collection<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities = storage.selectMultiple(new long[] {entityes.get(1).id()}, l0EntityClass);
        for (IEntity e : entities) {
            Assertions.assertEquals(l2EntityClass.ref(), e.entityClassRef());
        }
    }

    /**
     * 测试事务内查询,以测试事务隔离.
     */
    @Test
    public void testUncommittedTransactionSelect() throws Exception {
        IEntity uncommitEntity = Entity.Builder.anEntity()
            .withId(1000000)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .build();
        uncommitEntity.entityValue().addValues(
            Arrays.asList(
                new LongValue(l2EntityClass.field("l0-long").get(), 138293),
                new StringValue(l2EntityClass.field("l0-string").get(), "hAG7O1uv1FS3"),
                new StringsValue(l2EntityClass.field("l0-strings").get(), "KRW"),
                new EnumValue(l2EntityClass.field("l0-enum").get(), "Emerald"),
                new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("566017837.77")),
                new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                    LocalDateTime.of(2010, 6, 29, 9, 36, 36)),
                new LongValue(l2EntityClass.field("l1-long").get(), 072571712),
                new StringValue(l2EntityClass.field("l1-string").get(), "Logan_Uttridge6552@grannar.com"),
                new LongValue(l2EntityClass.field("l2-long").get(), 1448200874),
                new StringValue(l2EntityClass.field("l2-string").get(), "Benin")
            )
        );
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        Assertions.assertEquals(1, storage.build(uncommitEntity, l2EntityClass));

        Collection<EntityRef> refs = storage.select(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    l2EntityClass.field("l0-long").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(l2EntityClass.field("l0-long").get(), 138293))
            ),
            l0EntityClass,
            SelectConfig.Builder.anSelectConfig().withSort(Sort.buildOutOfSort()).withCommitId(0).build());
        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();

        Assertions.assertEquals(2, refs.size());
        long size = refs.stream().mapToLong(e -> e.getId()).filter(id -> (id == 1004) || (id == 1000000)).count();
        Assertions.assertEquals(2, size);
    }

    /**
     * 功能性查询测试.
     */
    @Test
    public void testSelect() throws Exception {
        // 确认没有事务.
        Assertions.assertFalse(transactionManager.getCurrent().isPresent());

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                refs = storage.select(c.conditions, c.entityClass,
                    SelectConfig.Builder.anSelectConfig()
                        .withDataAccessFitlerCondtitons(c.filterConditions)
                        .withCommitId(0)
                        .withSort(c.sort)
                        .withSecondarySort(c.secondSort)
                        .withThirdSort(c.thirdSort)
                        .build());
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.accept(refs);
        });
    }

    private Collection<Case> buildSelectCase() {

        return Arrays.asList(
            // not null查询
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.IS_NOT_NULL,
                            new EmptyTypedValue(l2EntityClass.field("l0-long").get())
                        )
                    ),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, r, false);
                }
            ),
            // is null查询
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.IS_NULL,
                            new EmptyTypedValue(l2EntityClass.field("l0-long").get())
                        )
                    ),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                    };
                    assertSelect(expectedIds, r, false);
                }
            ),
            // 查询指定附件.
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new AttachmentCondition(
                            l2EntityClass.field("l0-long").get(),
                            true,
                            "634274"
                        )
                    ),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                        1000
                    };
                    assertSelect(expectedIds, r, false);
                }
            ),
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new AttachmentCondition(
                            l2EntityClass.field("l0-long").get(),
                            false,
                            "634274"
                        )
                    ),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, r, false);
                },
                Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)
            ),
            // sort asc,按照id排序应该被优化掉.
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-enum").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(
                            l2EntityClass.field("l0-enum").get(), "Blue")
                    )),
                l2EntityClass,
                r -> {
                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, r, false);
                },
                Sort.buildOutOfSort()
            ),
            // id eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.EQUALS,
                        new LongValue(EntityField.ID_ENTITY_FIELD, 1004)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // id in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(EntityField.ID_ENTITY_FIELD, 1000),
                        new LongValue(EntityField.ID_ENTITY_FIELD, 1002)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1002
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // id in (not exist)
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        EntityField.ID_ENTITY_FIELD, ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(EntityField.ID_ENTITY_FIELD, 10000000)
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {};
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        l2EntityClass.field("l2-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l2-long").get(), 2032249908))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1003
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long not eq
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        l2EntityClass.field("l2-long").get(),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(l2EntityClass.field("l2-long").get(), 2032249908))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long >
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l1-long").get(),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(l2EntityClass.field("l1-long").get(), 87011006L))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-long").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l1-long").get(), 87011006L))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long <
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1002
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long <=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1002, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // long in
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-long").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 138293),
                        new LongValue(l2EntityClass.field("l0-long").get(), 634274),
                        new LongValue(l2EntityClass.field("l0-long").get(), 381134)
                    )),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // string eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // string no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // string like WILDCARD
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "org"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1002
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // string like SEGMENTATION
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        l2EntityClass.field("l2-string-segmentation").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l2-string-segmentation").get(), "市东城区东")
                    )
                ),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {1003};
                    assertSelect(expectedIds, result, false);
                }
            ),
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l0-string").get(), "or"))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // decimal eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // decimal no eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.NOT_EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // decimal >
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1001, 1002, 1003, 1004
                    };

                    assertSelect(expectedIds, result, false);

                }
            ),
            // decimal >=
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-decimal").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };

                    assertSelect(expectedIds, result, false);

                }
            ),
            // dateTimeField between
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-datetime").get(),
                        ConditionOperator.GREATER_THAN,
                        new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                            LocalDateTime.of(2001, 4, 29, 8, 13, 4))))

                    .addAnd(new Condition(l2EntityClass.field("l0-datetime").get(),
                        ConditionOperator.LESS_THAN,
                        new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                            LocalDateTime.of(2008, 6, 21, 19, 43, 51)))),
                l2EntityClass,
                result -> {

                    long[] expectedIds = {
                        1000
                    };

                    assertSelect(expectedIds, result, false);
                }
            ),
            // stringsField =
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1003
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // stringsField in
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB"),
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // emtpy condition
            new Case(Conditions.buildEmtpyConditions(),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1002, 1003, 1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // strings in no row.
            new Case(Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    l2EntityClass.field("l0-strings").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "iqoweiqweq"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "nbbbb"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {};
                    assertSelect(expectedIds, result, false);
                }
            ),
            // emtpy condition data access
            new Case(
                Conditions.buildEmtpyConditions(),
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l2-string").get(), "Mozambique")
                    )),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                },
                Sort.buildOutOfSort(),
                Sort.buildOutOfSort(),
                Sort.buildOutOfSort()
            ),
            // stringsField in with condition data access
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-strings").get(),
                            ConditionOperator.MULTIPLE_EQUALS,
                            new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB"),
                            new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l2-string").get(), "Trinidad")
                    ))
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l2-string").get(),
                            ConditionOperator.NOT_EQUALS,
                            new StringValue(l2EntityClass.field("l2-string").get(), "Lithuania")
                        )
                    ),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001
                    };
                    assertSelect(expectedIds, result, false);
                },
                Sort.buildOutOfSort(),
                Sort.buildOutOfSort(),
                Sort.buildOutOfSort()
            ),
            // bigint eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l2-bigint").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524951L)
                        )
                    ),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1004
                    };
                    assertSelect(expectedIds, result, false);
                }
            ),
            // Joint sorting
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY"))),
                l2EntityClass,
                result -> {
                    long[] expectedIds = {
                        1000, 1001, 1003
                    };
                    EntityRef[] expectedRefs = new EntityRef[] {
                        EntityRef.Builder.anEntityRef()
                            .withId(1000)
                            .withOrderValue("87011006")
                            .withSecondOrderValue("-2037817147")
                            .withThridOrderValue("5088141692596524950").build(),
                        EntityRef.Builder.anEntityRef()
                            .withId(1001)
                            .withOrderValue("443531115")
                            .withSecondOrderValue("-251454086")
                            .withThridOrderValue("5088141692596524949").build(),
                        EntityRef.Builder.anEntityRef()
                            .withId(1003)
                            .withOrderValue("647147145")
                            .withSecondOrderValue("2032249908")
                            .withThridOrderValue("5088141692596524947").build()

                    };
                    assertSelect(expectedIds, result, false);
                    List<EntityRef> targetRefs = new ArrayList(result);
                    for (int i = 0; i < expectedIds.length; i++) {
                        EntityRef targetRef = targetRefs.get(i);
                        EntityRef expectedRef = expectedRefs[i];

                        Assertions.assertEquals(expectedRef.getId(), targetRef.getId());
                        Assertions.assertEquals(expectedRef.getOrderValue(), targetRef.getOrderValue());
                        Assertions.assertEquals(expectedRef.getSecondOrderValue(), targetRef.getSecondOrderValue());
                        Assertions.assertEquals(expectedRef.getThridOrderValue(), targetRef.getThridOrderValue());
                    }
                },
                Sort.buildAscSort(l2EntityClass.field("l1-long").get()),
                Sort.buildAscSort(l2EntityClass.field("l2-long").get()),
                Sort.buildAscSort(l2EntityClass.field("l2-bigint").get())
            )
        );
    }

    private void assertSelect(long[] expectedIds, Collection<EntityRef> result, boolean sort) {
        Assertions.assertEquals(expectedIds.length, result.size());
        result.stream().forEach(e -> {
            Assertions
                .assertTrue(Arrays.binarySearch(expectedIds, e.getId()) >= 0, String.format("Not found %d", e.getId()));
            if (sort) {
                Assertions.assertNotNull(e.getOrderValue());
            }
        });
    }

    private static class Case {
        private Conditions conditions;
        private Conditions filterConditions;
        private IEntityClass entityClass;
        private Sort sort;
        private Sort secondSort;
        private Sort thirdSort;
        private Consumer<? super Collection<EntityRef>> check;

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check,
                Sort.buildOutOfSort(), Sort.buildOutOfSort(), Sort.buildOutOfSort());
        }

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check,
                    Sort sort) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check,
                sort, Sort.buildOutOfSort(), Sort.buildOutOfSort());
        }

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check,
                    Sort sort, Sort secondSort) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check,
                sort, secondSort, Sort.buildOutOfSort());
        }

        public Case(Conditions conditions, IEntityClass entityClass, Consumer<? super Collection<EntityRef>> check,
                    Sort sort, Sort secondSort, Sort thridSort) {
            this(conditions, Conditions.buildEmtpyConditions(), entityClass, check,
                sort, secondSort, thridSort);
        }

        public Case(
            Conditions conditions,
            Conditions filterConditions,
            IEntityClass entityClass,
            Consumer<? super Collection<EntityRef>> check,
            Sort sort,
            Sort secondSort,
            Sort thirdSort) {
            this.conditions = conditions;
            this.filterConditions = filterConditions;
            this.entityClass = entityClass;
            this.check = check;
            if (sort == null) {
                this.sort = Sort.buildOutOfSort();
            } else {
                this.sort = sort;
            }

            if (secondSort == null) {
                this.secondSort = Sort.buildOutOfSort();
            } else {
                this.secondSort = secondSort;
            }

            if (thirdSort == null) {
                this.thirdSort = Sort.buildOutOfSort();
            } else {
                this.thirdSort = thirdSort;
            }
        }
    }

    // 初始化数据
    private void initData(SQLMasterStorage storage) throws Exception {
        buildData();

        try {
            for (IEntity entity : entityes) {
                try {
                    storage.build(entity, l2EntityClass);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
                StorageInitialization.getInstance().getCommitIdStatusService().obsoleteAll();
            }
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }
    }


    private void buildData() {
        entityes = new ArrayList<>();

        long baseId = 1000;
        entityes.add(Entity.Builder.anEntity() // 1000
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 26, 12, 15, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 634274, "634274"),
                    new StringValue(l2EntityClass.field("l0-string").get(), "TjguZT2nz9KT"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "JPY", "USD"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Blue"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-792458736.36")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2003, 10, 29, 18, 55, 14)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 87011006),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Emely_Dickson1490@jiman.org"),
                    new LongValue(l2EntityClass.field("l2-long").get(), -2037817147),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Belize"),
                    new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524950L),
                    new StringValue(l2EntityClass.field("l2-string-segmentation").get(), "甘肃省兰州市安宁区兰州航空职工大学北校区旧教学楼101号")
                )
            ).build());

        entityes.add(Entity.Builder.anEntity() // 1001
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 27, 12, 15, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 381134, "381134"),
                    new StringValue(l2EntityClass.field("l0-string").get(), "qqSDo69gZcGW"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "JPY", "CHF"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Red"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("741808930.09")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2018, 11, 30, 11, 36, 41),
                        "2018-11-30 11:36:41"
                    ),
                    new LongValue(l2EntityClass.field("l1-long").get(), 443531115, "443531115"),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Manuel_Vincent2662@naiker.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), -251454086),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Montenegro"),
                    new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524949L),
                    new StringValue(l2EntityClass.field("l2-string-segmentation").get(), "太原市尖草坪区学院路3号182幢6612室B区")
                )
            ).build());

        entityes.add(Entity.Builder.anEntity() // 1002
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 129848, "129848"),
                    new StringValue(l2EntityClass.field("l0-string").get(), "oLS90hto8tSn"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB", "FRF", "AUD", "BEF"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Aqua"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-445124094.25")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2008, 6, 21, 19, 43, 51)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 208747364),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Maxwell_Richardson4862@twace.org"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 1457704562),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Lithuania"),
                    new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524948L),
                    new StringValue(l2EntityClass.field("l2-string-segmentation").get(), "北京市丰台区南四环西路188号9区2号楼7层")
                )
            ).build());

        entityes.add(Entity.Builder.anEntity() // 1003
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 333326, "333326"),
                    new StringValue(l2EntityClass.field("l0-string").get(), "Trm7n8Wd2ejj"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "JPY", "FIM"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Azure"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("659709035.95")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2001, 4, 29, 8, 13, 4)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 647147145),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Sebastian_Smith3123@sveldo.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 2032249908),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Trinidad"),
                    new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524947L),
                    new StringValue(l2EntityClass.field("l2-string-segmentation").get(), "北京市东城区东中街29号南写字楼第四层D-E号")
                )
            ).build());

        entityes.add(Entity.Builder.anEntity() // 1004
            .withId(baseId++)
            .withEntityClassRef(l2EntityClass.ref())
            .withTime(
                LocalDateTime.of(
                        2021, Month.FEBRUARY, 27, 12, 32, 20)
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
            .withVersion(0)
            .withMajor(OqsVersion.MAJOR)
            .withValues(
                Arrays.asList(
                    new LongValue(l2EntityClass.field("l0-long").get(), 138293, "138293"),
                    new StringValue(l2EntityClass.field("l0-string").get(), "H5qEkXkTvGWW"),
                    new StringsValue(l2EntityClass.field("l0-strings").get(), "KRW"),
                    new EnumValue(l2EntityClass.field("l0-enum").get(), "Lavender"),
                    new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("-304599899.25")),
                    new DateTimeValue(l2EntityClass.field("l0-datetime").get(),
                        LocalDateTime.of(2000, 4, 15, 20, 31, 58)),
                    new LongValue(l2EntityClass.field("l1-long").get(), 441034626),
                    new StringValue(l2EntityClass.field("l1-string").get(), "Alexia_Dillon5194@bauros.biz"),
                    new LongValue(l2EntityClass.field("l2-long").get(), 622028442),
                    new StringValue(l2EntityClass.field("l2-string").get(), "Mozambique"),
                    new LongValue(l2EntityClass.field("l2-bigint").get(), 5088141692596524951L),
                    new StringValue(l2EntityClass.field("l2-string-segmentation").get(),
                        "昆山市开发区柏庐南路1001号博悦万品大厦2号楼1208室")
                )
            ).build());
    }
}
