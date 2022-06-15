package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
     * 类型标识的开始值,依次递减.
     */
    private static long baseClassId = Long.MAX_VALUE;

    private static long l0EntityClassId = baseClassId;
    private static long l1EntityClassId = baseClassId - 1;
    private static long l2EntityClassId = baseClassId - 2;
    private static long userClassId = baseClassId - 3;
    private static long orderClassId = baseClassId - 4;
    private static long orderItemClassId = baseClassId - 5;

    private static long driverEntityClassId = baseClassId - 6;
    private static long lookupEntityClassId = baseClassId - 7;

    private static long odLookupOriginalEntityClassId = baseClassId - 8;
    private static long odLookupTargetEntityClassId = baseClassId - 9;
    private static long doLookupEntityClassId = baseClassId - 10;
    private static long doLookupTargetOriginalEntityClassId = baseClassId - 11;
    private static long ooLookupOriginalEntityClassId = baseClassId - 12;
    private static long ooLookupTargetOriginalEntityClassId = baseClassId - 13;

    public static IEntityClass L0_ENTITY_CLASS;
    public static IEntityClass L1_ENTITY_CLASS;
    public static IEntityClass L2_ENTITY_CLASS;
    public static IEntityClass DRIVER_ENTITY_CLASS;
    public static IEntityClass LOOKUP_ENTITY_CLASS;

    /*
     * 用户(用户名称, 用户编号, 订单总数count, 总消费金额sum, 平均消费金额avg, 最大消费金额max, 最小消费金额min)
     *   |---订单 (订单号, 下单时间, 订单项总数count, 总金额sum, 最大金额max, 最小金额min, 用户编号lookup, 总数量sum, 最大数量max, 最小数量min, 最大时间max,
     *   最小时间min, 平均数量avg, 订单项平均价格formula)
     *        |---订单项 (单号lookup, 物品名称, 金额, 数量, 时间)
     */
    public static IEntityClass USER_CLASS;
    public static IEntityClass ORDER_CLASS;
    public static IEntityClass ORDER_ITEM_CLASS;

    // 初始化使用，只有一个静态字段的简单订单
    public static IEntityClass SIMPLE_ORDER_CLASS;

    /*
    这是一组为了测试动静相互lookup的类型.
     */
    // 静态 lookup 动态
    public static IEntityClass OD_LOOKUP_ORIGINAL_ENTITY_CLASS;
    public static IEntityClass OD_LOOKUP_TARGET_ENTITY_CLASS;

    /**
     * 字段定义.
     */
    public static enum FieldId {
        l0LongFieldId,
        l0StringFieldId,
        l0StringsFieldId,
        l1LongFieldId,
        l1StringFieldId,
        l2StringFieldId,
        l2StringsFieldId,
        l2TimeFieldId,
        l2EnumFieldId,
        l2DecFieldId,
        l2Dec2FieldId,
        driverLongFieldId,
        lookupL2StringFieldId,
        lookupL0StringFieldId,
        lookupL2DecFieldId,
        l2LookupIdFieldId,
        l2LongFieldId,
        l2DriveId,
        l2OneToManyId,
        l2OneToManyLookupId,

        // 用户名称标识
        userNameFieldId,
        // 用户编号字段标识
        userCodeFieldId,
        // 用户订单总数字段标识
        userOrderTotalNumberCountFieldId,
        // 用户订单总消费金额字段标识.
        userOrderTotalPriceSumFieldId,
        // 用户订单平均消费金额字段标识.
        userOrderAvgPriceAvgFieldId,
        // 用户订单最大消费金额字段标识.
        userOrderAvgPriceMaxFieldId,
        // 用户订单最小消费金额字段标识.
        userOrderAvgPriceMinFieldId,
        // 用户订单关联字段标识.
        orderUserForeignFieldId,
        // 订单编号字段标识.
        orderCodeFieldId,
        // 订单下单时间字段标识.
        orderCreateTimeFieldId,
        // 订单项总数
        orderTotalNumberCountFieldId,
        // 订单总金额.
        orderTotalPriceSumFieldId,
        // 订单最小金额.
        orderMinPriceSumFieldId,
        // 订单最大金额.
        orderMaxPriceSumFieldId,
        // 订单总数量.
        orderTotalNumSumFieldId,
        // 订单最大数量.
        orderTotalNumMaxFieldId,
        // 订单最小数量.
        orderTotalNumMinFieldId,
        // 订单平均数量.
        orderTotalNumAvgFieldId,
        // 订单最大时间.
        orderTotalTimeMaxFieldId,
        // 订单最小时间.
        orderTotalTimeMinFieldId,
        // 订单lookup用户编号.
        orderUserCodeLookupFieldId,
        // 订单-订单项外键标识.
        orderOrderItemForeignFieldId,
        // 订单项lookup订单号字段标识.
        orderItemOrderCodeLookupFieldId,
        // 订单项名称字段标识.
        orderItemNameFieldId,
        // 订单项金额字段标识.
        orderItemPriceFieldId,
        // 订单的订单项平均价格.
        orderAvgPriceFieldId,
        // 订单项数量字段标识.
        orderItemNumFieldId,
        // 订单项时间字段标识.
        orderItemTimeFieldId,

        // 静态-> 动态 的静态发起lookup字段.
        odLookupFieldId,
        // 静态-> 动态 的动态被lookup字段.
        odLookupTargetFieldId,
        // 静态-> 动态 外键字段
        odLookupForeignFieldId,
        // 动态-> 静态 的动态发起lookup字段.
        doLookupFieldId,
        // 动态-> 静态 的被lookup字段.
        doLookupTargetFieldId,
        // 静态 -> 静态 的静态发起lookup字段.
        ooLookupFieldId,
        // 静态 -> 静态 的静态被lookup字段.
        ooLookupTargetFieldId,
    }

    // 用户订单关系字段.
    private static IEntityField orderUserForeignField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - FieldId.orderUserForeignFieldId.ordinal())
        .withName("订单用户关联")
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withSearchable(true).build()
        ).build();

    // 订单订单项关系字段.
    private static IEntityField orderOrderItemForeignField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - FieldId.orderOrderItemForeignFieldId.ordinal())
        .withName("订单项订单关联")
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withSearchable(true).build()
        ).build();

    // 静态->动态 关系字段.
    private static IEntityField odLookupForeignField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - FieldId.odLookupForeignFieldId.ordinal())
        .withName("静态动态lookup关联")
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
                    .withId(Long.MAX_VALUE - FieldId.l0LongFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("l0-long")
                    .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l0StringFieldId.ordinal())
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
                .withId(Long.MAX_VALUE - FieldId.l0StringsFieldId.ordinal())
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
                .withId(Long.MAX_VALUE - FieldId.l1LongFieldId.ordinal())
                .withFieldType(FieldType.LONG)
                .withName("l1-long")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l1StringFieldId.ordinal())
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
                .withId(Long.MAX_VALUE - FieldId.l2StringsFieldId.ordinal())
                .withFieldType(FieldType.STRINGS)
                .withName("l2-strings")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2StringFieldId.ordinal())
                .withFieldType(FieldType.STRING)
                .withName("l2-string")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2TimeFieldId.ordinal())
                .withFieldType(FieldType.DATETIME)
                .withName("l2-time")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(Integer.MAX_VALUE).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2EnumFieldId.ordinal())
                .withFieldType(FieldType.ENUM)
                .withName("l2-enum")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2DecFieldId.ordinal())
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig().withLen(100).withPrecision(6).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2Dec2FieldId.ordinal())
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec2")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig().withLen(100).withPrecision(6).withSearchable(true).build())
                .build())
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.l2LongFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("l2-long")
                    .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - FieldId.l2DriveId.ordinal())
                .withFieldType(FieldType.LONG)
                .withName("l2-driver.id")
                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                .build())
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(Long.MAX_VALUE - FieldId.l2OneToManyId.ordinal())
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.l2DriveId.ordinal())
                                .withFieldType(FieldType.LONG)
                                .withName("l2-driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(L2_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(Long.MAX_VALUE - FieldId.l2DriveId.ordinal())
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.l2DriveId.ordinal())
                                .withFieldType(FieldType.LONG)
                                .withName("l2-driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(DRIVER_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(DRIVER_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                        .withCode("l2-one-to-many-lookup")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withStrong(true)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                                .withName("l2-lookup.id")
                                .withFieldType(FieldType.LONG)
                                .withConfig(
                                    FieldConfig.Builder.anFieldConfig().withSearchable(true)
                                        .withLen(Integer.MAX_VALUE).build()
                                ).build()
                        )
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withRightEntityClassId(lookupEntityClassId)
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(LOOKUP_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(LOOKUP_ENTITY_CLASS))
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
                    .withId(Long.MAX_VALUE - FieldId.driverLongFieldId.ordinal())
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
                        .withId(Long.MAX_VALUE - FieldId.l2OneToManyId.ordinal())
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("driver")
                        .withEntityField(L2_ENTITY_CLASS.field("l2-driver.id").get())
                        .withRightEntityClassId(L2_ENTITY_CLASS.id())
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(L2_ENTITY_CLASS))
                        .build()
                )
            ).build();

        LOOKUP_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(lookupEntityClassId)
            .withLevel(0)
            .withCode("lookup")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.lookupL2StringFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.lookupL0StringFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.lookupL2DecFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                    .withName("l2-lookup.id")
                    .withFieldType(FieldType.LONG)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig().withSearchable(true).withLen(Integer.MAX_VALUE).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(Long.MAX_VALUE - FieldId.l2OneToManyLookupId.ordinal())
                        .withCode("l2-one-to-many-lookup")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withStrong(true)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                                .withName("l2-lookup.id")
                                .withFieldType(FieldType.LONG)
                                .withConfig(
                                    FieldConfig.Builder.anFieldConfig().withSearchable(true)
                                        .withLen(Integer.MAX_VALUE).build()
                                ).build()
                        )
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withRightEntityClassId(lookupEntityClassId)
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(LOOKUP_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(LOOKUP_ENTITY_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                        .withCode("l2-many-to-one-lookup")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withStrong(true)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.l2LookupIdFieldId.ordinal())
                                .withName("l2-lookup.id")
                                .withFieldType(FieldType.LONG)
                                .withConfig(
                                    FieldConfig.Builder.anFieldConfig().withSearchable(true)
                                        .withLen(Integer.MAX_VALUE).build()
                                ).build()
                        )
                        .withLeftEntityClassId(lookupEntityClassId)
                        .withLeftEntityClassCode("lookup")
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id, a) -> Optional.ofNullable(L2_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(L2_ENTITY_CLASS))
                        .build()

                )
            ).build();

        USER_CLASS = EntityClass.Builder.anEntityClass()
            .withId(userClassId)
            .withCode("user")
            .withLevel(0)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.userNameFieldId.ordinal())
                    .withFieldType(FieldType.STRING)
                    .withName("用户名称")
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
                    .withId(Long.MAX_VALUE - FieldId.userCodeFieldId.ordinal())
                    .withFieldType(FieldType.STRING)
                    .withName("用户编号")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(100)
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.NOT)
                            .withFieldSense(FieldConfig.FieldSense.NORMAL)
                            .withRequired(false).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.userOrderTotalNumberCountFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.userOrderTotalPriceSumFieldId.ordinal())
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
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderTotalPriceSumFieldId.ordinal())
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.userOrderAvgPriceAvgFieldId.ordinal())
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
                                    .withClassId(orderClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderTotalPriceSumFieldId.ordinal())
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.userOrderAvgPriceMaxFieldId.ordinal())
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
                                    .withClassId(orderClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderTotalPriceSumFieldId.ordinal())
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.userOrderAvgPriceMinFieldId.ordinal())
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
                                    .withClassId(orderClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderTotalPriceSumFieldId.ordinal())
                                    .withRelationId(orderUserForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id() - 100)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withLeftEntityClassId(userClassId)
                        .withLeftEntityClassCode("user")
                        .withRightEntityClassId(orderClassId)
                        .withRightEntityClassLoader((orderClassId, a) -> Optional.of(ORDER_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(ORDER_CLASS))
                        .withEntityField(orderUserForeignField).build()
                )
            ).build();

        SIMPLE_ORDER_CLASS = EntityClass.Builder.anEntityClass()
                .withId(orderClassId)
                .withCode("order")
                .withLevel(0)
                .withField(
                        EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - FieldId.orderCreateTimeFieldId.ordinal())
                                .withFieldType(FieldType.DATETIME)
                                .withName("下单时间")
                                .withConfig(
                                        FieldConfig.Builder.anFieldConfig()
                                                .withSearchable(true)
                                                .withFuzzyType(FieldConfig.FuzzyType.NOT)
                                                .withFieldSense(FieldConfig.FieldSense.NORMAL)
                                                .withRequired(true).build()
                                ).build()
                ).build();

        ORDER_CLASS = EntityClass.Builder.anEntityClass()
            .withId(orderClassId)
            .withCode("order")
            .withLevel(0)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderCodeFieldId.ordinal())
                    .withFieldType(FieldType.STRING)
                    .withName("订单号")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(100)
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.NOT)
                            .withFieldSense(FieldConfig.FieldSense.NORMAL)
                            .withRequired(false)
                            .withCalculation(
                                AutoFill.Builder.anAutoFill()
                                    .withPatten("{0000}")
                                    .withDomainNoType(AutoFill.DomainNoType.NORMAL)
                                    .withLevel(1)
                                    .build()
                            )
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderCreateTimeFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.orderTotalNumberCountFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.orderTotalPriceSumFieldId.ordinal())
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
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemPriceFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderMaxPriceSumFieldId.ordinal())
                    .withFieldType(FieldType.DECIMAL)
                    .withName("最大金额max")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MAX)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemPriceFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderMinPriceSumFieldId.ordinal())
                    .withFieldType(FieldType.DECIMAL)
                    .withName("最小金额min")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MIN)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemPriceFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderUserCodeLookupFieldId.ordinal())
                    .withFieldType(FieldType.STRING)
                    .withName("用户编号lookup")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(100)
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(userClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.userCodeFieldId.ordinal()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalNumSumFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("总数量sum")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(0)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.SUM)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemNumFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalNumMaxFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("最大数量max")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(0)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MAX)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemNumFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalNumMinFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("最小数量min")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(0)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MIN)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemNumFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalTimeMaxFieldId.ordinal())
                    .withFieldType(FieldType.DATETIME)
                    .withName("最大时间max")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(0)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MAX)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemTimeFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalTimeMinFieldId.ordinal())
                    .withFieldType(FieldType.DATETIME)
                    .withName("最小时间min")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(0)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.MIN)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemTimeFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderTotalNumAvgFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("平均数量avg")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withLen(19)
                            .withPrecision(6)
                            .withSearchable(true)
                            .withCalculation(
                                Aggregation.Builder.anAggregation()
                                    .withAggregationType(AggregationType.AVG)
                                    .withClassId(orderItemClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderItemNumFieldId.ordinal())
                                    .withRelationId(orderOrderItemForeignField.id()).build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderAvgPriceFieldId.ordinal())
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
                        .withId(orderOrderItemForeignField.id())
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(orderItemClassId)
                        .withRightEntityClassLoader((orderItemClassId, a) -> Optional.of(ORDER_ITEM_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(ORDER_ITEM_CLASS))
                        .withEntityField(orderOrderItemForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id())
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withStrong(true)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(userClassId)
                        .withRightEntityClassLoader((userClassId, a) -> Optional.of(USER_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(USER_CLASS))
                        .withEntityField(orderUserForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderUserForeignField.id() - 200)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withLeftEntityClassId(orderClassId)
                        .withLeftEntityClassCode("order")
                        .withRightEntityClassId(userClassId)
                        .withRightEntityClassLoader((userClassId, a) -> Optional.of(USER_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(USER_CLASS))
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
                    .withId(Long.MAX_VALUE - FieldId.orderItemOrderCodeLookupFieldId.ordinal())
                    .withName("单号lookup")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(100)
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(orderClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.orderCodeFieldId.ordinal())
                                    .build()
                            ).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderItemNameFieldId.ordinal())
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
                    .withId(Long.MAX_VALUE - FieldId.orderItemPriceFieldId.ordinal())
                    .withFieldType(FieldType.DECIMAL)
                    .withName("金额")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(19)
                            .withPrecision(6).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderItemNumFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("数量")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(19)
                            .withPrecision(6).build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.orderItemTimeFieldId.ordinal())
                    .withFieldType(FieldType.DATETIME)
                    .withName("时间")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(19).build()
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
                        .withRightEntityClassLoader((orderClassId, a) -> Optional.of(ORDER_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(ORDER_CLASS))
                        .withEntityField(orderOrderItemForeignField).build(),
                    Relationship.Builder.anRelationship()
                        .withId(orderOrderItemForeignField.id() - 200)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withLeftEntityClassId(orderItemClassId)
                        .withLeftEntityClassCode("orderItem")
                        .withRightEntityClassId(orderClassId)
                        .withRightEntityClassLoader((orderClassId, a) -> Optional.of(ORDER_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(ORDER_CLASS))
                        .withEntityField(orderOrderItemForeignField).build()
                )
            ).build();

        OD_LOOKUP_ORIGINAL_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(odLookupOriginalEntityClassId)
            .withCode("od_lookup_original")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.odLookupFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("od-lookup-original-long")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .withJdbcType(Types.BIGINT)
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(odLookupTargetEntityClassId)
                                    .withFieldId(Long.MAX_VALUE - FieldId.odLookupTargetFieldId.ordinal())
                                    .build()
                            ).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(odLookupForeignField.id())
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withStrong(true)
                        .withLeftEntityClassId(odLookupOriginalEntityClassId)
                        .withLeftEntityClassCode("od_lookup_original")
                        .withRightEntityClassId(odLookupTargetEntityClassId)
                        .withRightEntityClassLoader((classId, a) -> Optional.of(OD_LOOKUP_TARGET_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(OD_LOOKUP_TARGET_ENTITY_CLASS))
                        .withEntityField(odLookupForeignField).build()
                )
            ).build();

        OD_LOOKUP_TARGET_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
            .withId(odLookupTargetEntityClassId)
            .withCode("od_lookup_target")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - FieldId.odLookupTargetFieldId.ordinal())
                    .withFieldType(FieldType.LONG)
                    .withName("od-lookup-target-long")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE).build()
                    ).build()
            ).withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(odLookupForeignField.id())
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withLeftEntityClassId(odLookupTargetEntityClassId)
                        .withLeftEntityClassCode("od_lookup_target")
                        .withRightEntityClassId(odLookupOriginalEntityClassId)
                        .withRightEntityClassLoader((classId, a) -> Optional.of(OD_LOOKUP_ORIGINAL_ENTITY_CLASS))
                        .withRightFamilyEntityClassLoader((id) -> Arrays.asList(OD_LOOKUP_ORIGINAL_ENTITY_CLASS))
                        .withEntityField(odLookupForeignField).build()
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
            ORDER_ITEM_CLASS,

            /*
            专门为了动静之间测试准备的对象.
            OD 表示静态->动态
            DO 表示动态->静态.
            OO 表示静态->静态.
             */

            // 静态 lookup 动态
            OD_LOOKUP_ORIGINAL_ENTITY_CLASS,
            OD_LOOKUP_TARGET_ENTITY_CLASS,
        };

        for (IEntityClass e : es) {
            Mockito.when(metaManager.load(e.id(), "")).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.id(), OqsProfile.UN_DEFINE_PROFILE)).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.id(), null)).thenReturn(Optional.of(e));
            Mockito.when(metaManager.load(e.ref())).thenReturn(Optional.of(e));
        }
        Mockito.when(metaManager.appLoad(Mockito.anyString())).thenReturn(Stream.of(ORDER_CLASS).collect(Collectors.toList()));

    }

    /**
     * 模拟部署新对象.
     */
    public static void changeOrder(MetaManager metaManager) {
        Mockito.when(metaManager.load(SIMPLE_ORDER_CLASS.id(), "")).thenReturn(Optional.of(SIMPLE_ORDER_CLASS));
        Mockito.when(metaManager.load(SIMPLE_ORDER_CLASS.id(), OqsProfile.UN_DEFINE_PROFILE)).thenReturn(Optional.of(SIMPLE_ORDER_CLASS));
        Mockito.when(metaManager.load(SIMPLE_ORDER_CLASS.id(), null)).thenReturn(Optional.of(SIMPLE_ORDER_CLASS));
        Mockito.when(metaManager.load(SIMPLE_ORDER_CLASS.ref())).thenReturn(Optional.of(SIMPLE_ORDER_CLASS));
    }

    /**
     * 默认自动编号.
     */
    public static SegmentInfo getDefaultSegmentInfo() {
        return SegmentInfo.builder().withVersion(0L)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withStep(1000)
            .withPatten("{0000}")
            .withMode(AutoFill.DomainNoType.NORMAL.getType())
            .withMaxId(9999L)
            .withBizType(String.valueOf(Long.MAX_VALUE - FieldId.orderCodeFieldId.ordinal()))
            .withPatternKey("")
            .withResetable(1)
            .withBeginId(1L)
            .build();
    }
}

