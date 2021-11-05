package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;

/**
 * MetaManager的Mock实现,为了单元测试准备.
 *
 * @author dongbin
 * @version 0.1 2021/3/18 15:12
 * @since 1.8
 */
@Disabled("explanation")
public class MockEntityClassDefine {

    public static long DRIVCER_ID_FEILD_ID = Long.MAX_VALUE;
    /**
     * 关系标识的开始值,依次递减.
     */
    private static long baseRelationsId = Integer.MAX_VALUE;
    /**
     * 类型标识的开始值,依次递减.
     */
    private static long baseClassId = Long.MAX_VALUE;
    /**
     * 字段的标识开始值,依次递减.
     */
    private static long baseFieldId = Long.MAX_VALUE - 1;

    private static long l0EntityClassId = baseClassId;
    private static long l1EntityClassId = baseClassId - 1;
    private static long l2EntityClassId = baseClassId - 2;
    private static long userClassId = baseClassId - 3;
    private static long orderClassId = baseClassId - 4;
    private static long orderItemClassId = baseClassId - 5;

    private static long driverEntityClassId = baseClassId - 6;
    private static long lookupEntityClassId = baseClassId - 7;

    private static long l0LongFieldId = baseFieldId - 1;
    private static long l0StringFieldId = baseFieldId - 2;
    private static long l0StringsFieldId = baseFieldId - 3;
    private static long l1LongFieldId = baseFieldId - 4;
    private static long l1StringFieldId = baseFieldId - 5;
    private static long l2StringFieldId = baseFieldId - 6;
    private static long l2TimeFieldId = baseFieldId - 7;
    private static long l2EnumFieldId = baseFieldId - 8;
    private static long l2DecFieldId = baseFieldId - 9;
    private static long driverLongFieldId = baseFieldId - 10;
    private static long lookupL2StringFieldId = baseFieldId - 11;
    private static long lookupL0StringFieldId = baseFieldId - 12;
    private static long lookupL2DecFieldId = baseFieldId - 13;
    private static long l2LookupIdFieldId = baseFieldId - 14;


    public static IEntityClass L0_ENTITY_CLASS;
    public static IEntityClass L1_ENTITY_CLASS;
    public static IEntityClass L2_ENTITY_CLASS;
    public static IEntityClass DRIVER_ENTITY_CLASS;
    public static IEntityClass LOOKUP_ENTITY_CLASS;

    /*
     * 用户(用户编号, 订单总数(count), 总消费金额(sum), 平均消费金额(avg))
     *   |---订单 (单号, 下单时间, 订单项总数(count), 总金额(sum), 用户编号(lookup))
     *        |---订单项 (单号(lookup), 物品名称, 金额)
     */
    public static IEntityClass USER_CLASS;
    public static IEntityClass ORDER_CLASS;
    public static IEntityClass ORDER_ITEM_CLASS;

    // 用户编号字段标识
    private static long userCodeFieldId = baseFieldId - 15;
    // 用户订单总数字段标识
    private static long userOrderTotalNumberCountFieldId = baseFieldId - 16;
    // 用户订单总消费金额字段标识.
    private static long userOrderTotalPriceSumFieldId = baseFieldId - 17;
    // 用户订单平均消费金额字段标识.
    private static long userOrderAvgPriceAvgFieldId = baseFieldId - 18;
    // 用户订单最大消费金额字段标识.
    private static long userOrderAvgPriceMaxFieldId = baseFieldId - 181;
    // 用户订单最小消费金额字段标识.
    private static long userOrderAvgPriceMinFieldId = baseFieldId - 182;
    // 用户订单关联字段标识.
    private static long orderUserForeignFieldId = baseFieldId - 19;
    // 订单编号字段标识.
    private static long orderCodeFieldId = baseFieldId - 20;
    // 订单下单时间字段标识.
    private static long orderCreateTimeFieldId = baseFieldId - 21;
    // 订单项总数
    private static long orderTotalNumberCountFieldId = baseFieldId - 22;
    // 订单总金额.
    private static long orderTotalPriceSumFieldId = baseFieldId - 23;
    // 订单lookup用户编号.
    private static long orderUserCodeLookupFieldId = baseFieldId - 24;
    // 订单-订单项外键标识.
    private static long orderOrderItemForeignFieldId = baseFieldId - 25;
    // 订单项lookup订单号字段标识.
    private static long orderItemOrderCodeLookupFieldId = baseFieldId - 26;
    // 订单项名称字段标识.
    private static long orderItemNameFieldId = baseFieldId - 27;
    // 订单项金额字段标识.
    private static long orderItemPriceFieldId = baseFieldId - 28;
    // 订单的订单项平均价格.
    private static long orderAvgPriceFieldId = baseFieldId - 29;

    // 用户订单关系字段.
    private static IEntityField orderUserForeignField = EntityField.Builder.anEntityField()
        .withId(orderUserForeignFieldId)
        .withName("订单用户关联")
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withSearchable(true).build()
        ).build();

    // 订单订单项关系字段.
    private static IEntityField orderOrderItemForeignField = EntityField.Builder.anEntityField()
        .withId(orderOrderItemForeignFieldId)
        .withName("订单项订单关联")
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withSearchable(true).build()
        ).build();

    static {
        L0_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(l0EntityClassId)
            .withLevel(0)
            .withCode("l0")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(l0LongFieldId)
                    .withFieldType(FieldType.LONG)
                    .withName("l0-long")
                    .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(l0StringFieldId)
                .withFieldType(FieldType.STRING)
                .withName("l0-string")
                .withConfig(FieldConfig
                    .Builder.anFieldConfig().withLen(128)
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                    .build())
                .build()
            )
            .withField(EntityField.Builder.anEntityField()
                .withId(l0StringsFieldId)
                .withFieldType(FieldType.STRINGS)
                .withName("l0-strings")
                .withConfig(FieldConfig
                    .Builder.anFieldConfig().withLen(128)
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                    .build())
                .build()
            )
            .build();

        L1_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(l1EntityClassId)
            .withLevel(1)
            .withCode("l1")
            .withField(EntityField.Builder.anEntityField()
                .withId(l1LongFieldId)
                .withFieldType(FieldType.LONG)
                .withName("l1-long")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(l1StringFieldId)
                .withFieldType(FieldType.STRING)
                .withName("l1-string")
                .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build())
            .withFather(L0_ENTITY_CLASS)
            .build();

        L2_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(l2EntityClassId)
            .withLevel(2)
            .withCode("l2")
            .withField(EntityField.Builder.anEntityField()
                .withId(l2StringFieldId)
                .withFieldType(FieldType.STRING)
                .withName("l2-string")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(l2TimeFieldId)
                .withFieldType(FieldType.DATETIME)
                .withName("l2-time")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(Integer.MAX_VALUE).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(l2EnumFieldId)
                .withFieldType(FieldType.ENUM)
                .withName("l2-enum")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(l2DecFieldId)
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig().withLen(100).withPrecision(6).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(DRIVCER_ID_FEILD_ID)
                .withFieldType(FieldType.LONG)
                .withName("l2-driver.id")
                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                .build())
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(DRIVCER_ID_FEILD_ID)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(DRIVCER_ID_FEILD_ID)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(DRIVCER_ID_FEILD_ID)
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(DRIVCER_ID_FEILD_ID)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(DRIVER_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(baseRelationsId - 2)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withStrong(true)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withRightEntityClassId(lookupEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(LOOKUP_ENTITY_CLASS))
                        .build()
                )
            )
            .withFather(L1_ENTITY_CLASS)
            .build();

        DRIVER_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(driverEntityClassId)
            .withLevel(0)
            .withCode("driver")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(driverLongFieldId)
                    .withFieldType(FieldType.LONG)
                    .withName("driver-long")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(baseRelationsId - 3)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("driver")
                        .withEntityField(L2_ENTITY_CLASS.field("l2-driver.id").get())
                        .withRightEntityClassId(L2_ENTITY_CLASS.id())
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(baseRelationsId - 4)
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(L2_ENTITY_CLASS.id())
                        .withLeftEntityClassCode(L2_ENTITY_CLASS.code())
                        .withEntityField(L2_ENTITY_CLASS.field("l2-driver.id").get())
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(DRIVER_ENTITY_CLASS))
                        .build()
                )
            ).build();

        LOOKUP_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(lookupEntityClassId)
            .withLevel(0)
            .withCode("lookup")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(lookupL2StringFieldId)
                    .withName("lookup-l2-string")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(L2_ENTITY_CLASS.field("l2-string").get().id()).build()
                            )
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(lookupL0StringFieldId)
                    .withName("lookup-l0-string")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(L2_ENTITY_CLASS.field("l0-string").get().id()).build()
                            )
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(lookupL2DecFieldId)
                    .withName("lookup-l2-dec")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(L2_ENTITY_CLASS.field("l2-dec").get().id()).build()
                            )
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .withPrecision(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(l2LookupIdFieldId)
                    .withName("l2-lookup.id")
                    .withFieldType(FieldType.LONG)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig().withSearchable(true).withLen(Integer.MAX_VALUE).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(baseRelationsId - 5)
                        .withCode("l2-one-to-many-lookup")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withStrong(true)
                        .withLeftEntityClassId(lookupEntityClassId)
                        .withLeftEntityClassCode("lookup")
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .build()
                )
            ).build();

        USER_CLASS = EntityClass.Builder.anEntityClass()
            .withId(userClassId)
            .withCode("user")
            .withLevel(0)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userCodeFieldId)
                    .withFieldType(FieldType.STRING)
                    .withName("用户编号")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(100)
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.NOT)
                            .withFieldSense(FieldConfig.FieldSense.NORMAL)
                            .withRequired(true).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userOrderTotalNumberCountFieldId)
                    .withName("订单总数count")
                    .withFieldType(FieldType.LONG)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.COUNT)
                                    .withClassId(orderClassId)
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userOrderTotalPriceSumFieldId)
                    .withName("总消费金额sum")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withSearchable(true)
                            .withPrecision(6)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.SUM)
                                    .withClassId(orderClassId)
                                    .withFieldId(orderTotalPriceSumFieldId)
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userOrderAvgPriceAvgFieldId)
                    .withName("平均消费金额avg")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.AVG)
                                    .withConditions(Conditions.buildEmtpyConditions())
                                    .withClassId(orderClassId)
                                    .withFieldId(orderTotalPriceSumFieldId)
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userOrderAvgPriceMaxFieldId)
                    .withName("最大消费金额max")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                     .withAggregationType(AggregationType.MAX)
                                     .withConditions(Conditions.buildEmtpyConditions())
                                     .withClassId(orderClassId)
                                     .withFieldId(orderTotalPriceSumFieldId)
                                     .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(userOrderAvgPriceMinFieldId)
                    .withName("最小消费金额min")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                         FieldConfig.Builder.anFieldConfig()
                              .withLen(19)
                              .withPrecision(6)
                              .withSearchable(true)
                              .withCalculation(
                                   Aggregation.Builder.anAggregation()
                                        .withAggregationType(AggregationType.MIN)
                                        .withConditions(Conditions.buildEmtpyConditions())
                                        .withClassId(orderClassId)
                                        .withFieldId(orderTotalPriceSumFieldId)
                                        .withRelationId(orderUserForeignField.id()).build()
                              ).build()
                    ).build()
                )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id() + 100)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withLeftEntityClassId(userClassId)
                        .withLeftEntityClassCode("user")
                        .withRightEntityClassId(orderClassId)
                        .withRightEntityClassLoader(orderClassId -> Optional.of(ORDER_CLASS))
                        .withEntityField(orderUserForeignField).build()
                )
            ).build();

        ORDER_CLASS = EntityClass.Builder.anEntityClass()
            .withId(orderClassId)
            .withCode("order")
            .withLevel(0)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderCodeFieldId)
                    .withFieldType(FieldType.STRING)
                    .withName("订单号")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(100)
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.NOT)
                            .withFieldSense(FieldConfig.FieldSense.NORMAL)
                            .withRequired(true).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderCreateTimeFieldId)
                    .withFieldType(FieldType.DATETIME)
                    .withName("下单时间")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.NOT)
                            .withFieldSense(FieldConfig.FieldSense.NORMAL)
                            .withRequired(true).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderTotalNumberCountFieldId)
                    .withFieldType(FieldType.LONG)
                    .withName("订单项总数count")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.COUNT)
                                    .withClassId(orderItemClassId)
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderTotalPriceSumFieldId)
                    .withFieldType(FieldType.DECIMAL)
                    .withName("总金额sum")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.SUM)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(orderItemPriceFieldId)
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderUserCodeLookupFieldId)
                    .withFieldType(FieldType.STRING)
                    .withName("用户编号lookup")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(100)
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(userClassId)
                                    .withFieldId(userCodeFieldId).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderAvgPriceFieldId)
                    .withFieldType(FieldType.DECIMAL)
                    .withName("订单项平均价格formula")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withScale(1)
                            .withCalculation(Formula.Builder.anFormula()
                                .withLevel(1)
                                .withExpression("${总金额sum} / ${订单项总数count}")
                                .withFailedDefaultValue(new BigDecimal("0.0"))
                                .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                                .withArgs(Arrays.asList("总金额sum", "订单项总数count"))
                                .build()
                            ).build()
                    )
                    .build()
            )
            .withField(orderUserForeignField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(orderOrderItemForeignField.id() + 100)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(orderItemClassId)
                        .withRightEntityClassLoader(orderItemClassId -> Optional.of(ORDER_ITEM_CLASS))
                        .withEntityField(orderOrderItemForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id())
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(userClassId)
                        .withRightEntityClassLoader(userClassId -> Optional.of(USER_CLASS))
                        .withEntityField(orderUserForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id() + 200)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(userClassId)
                        .withRightEntityClassLoader(userClassId -> Optional.of(USER_CLASS))
                        .withEntityField(orderUserForeignField).build()

                )
            )
            .build();

        ORDER_ITEM_CLASS = EntityClass.Builder.anEntityClass()
            .withId(orderItemClassId)
            .withCode("orderItem")
            .withLevel(0)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderItemOrderCodeLookupFieldId)
                    .withName("单号lookup")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(100)
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(orderClassId)
                                    .withFieldId(orderCodeFieldId)
                                    .build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderItemNameFieldId)
                    .withFieldType(FieldType.STRING)
                    .withName("物品名称")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(100)
                            .withSearchable(true).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(orderItemPriceFieldId)
                    .withFieldType(FieldType.DECIMAL)
                    .withName("金额")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(19)
                            .withPrecision(6).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(orderOrderItemForeignField.id())
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(orderItemClassId)
                        .withLeftEntityClassCode("orderItem")
                        .withRightEntityClassId(orderClassId)
                        .withRightEntityClassLoader(orderClassId -> Optional.of(ORDER_CLASS))
                        .withEntityField(orderOrderItemForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderOrderItemForeignField.id() + 200)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withLeftEntityClassId(orderItemClassId)
                        .withLeftEntityClassCode("orderItem")
                        .withRightEntityClassId(orderClassId)
                        .withRightEntityClassLoader(orderClassId -> Optional.of(ORDER_CLASS))
                        .withEntityField(orderOrderItemForeignField).build()
                )
            ).build();
    }

    /**
     * 初始化.
     */
    public static void initMetaManager(MetaManager metaManager) {
        IEntityClass[] es = new IEntityClass[] {
            L0_ENTITY_CLASS,
            L1_ENTITY_CLASS,
            L2_ENTITY_CLASS,
            DRIVER_ENTITY_CLASS,
            LOOKUP_ENTITY_CLASS,
            USER_CLASS,
            ORDER_CLASS,
            ORDER_ITEM_CLASS
        };

        for (IEntityClass e : es) {
            Mockito.when(metaManager.load(e.id())).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.id(), OqsProfile.UN_DEFINE_PROFILE)).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.id(), null)).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.ref())).thenReturn(Optional.of(e));
        }
    }
}
