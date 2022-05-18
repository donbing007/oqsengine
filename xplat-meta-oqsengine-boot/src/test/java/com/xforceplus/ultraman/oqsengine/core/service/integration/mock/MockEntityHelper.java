package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Locale;

/**
 * 对象构造帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/1/18 10:48
 * @since 1.8
 */
public class MockEntityHelper {

    private Faker faker = new Faker(Locale.CHINA);
    private LongIdGenerator idGenerator;

    public MockEntityHelper(LongIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 构造一个 MockEntityClassDefine.USER_CLASS 的实例.
     */
    public IEntity buildUserEntity() {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.USER_CLASS.ref())
            .withValue(
                new StringValue(
                    MockEntityClassDefine.USER_CLASS.field("用户编号").get(),
                    "U" + idGenerator.next())
            )
            .withValue(
                new StringValue(
                    MockEntityClassDefine.USER_CLASS.field("用户名称").get(),
                    faker.name().name())
            )
            .build();
    }

    /**
     * 构造一个 MockEntityClassDefine.ORDER_CLASS 实例.
     *
     * @param user 相关联的 MockEntityClassDefine.USER_CLASS 实例.
     */
    public IEntity buildOrderEntity(IEntity user) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.ORDER_CLASS.ref())
            .withValue(
                new StringValue(
                    MockEntityClassDefine.ORDER_CLASS.field("订单号").get(),
                    "O" + idGenerator.next()
                )
            )
            .withValue(
                new DateTimeValue(
                    MockEntityClassDefine.ORDER_CLASS.field("下单时间").get(),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )
            )
            .withValue(
                new LookupValue(
                    MockEntityClassDefine.ORDER_CLASS.field("用户编号lookup").get(),
                    user.id()
                )
            )
            .withValue(
                new LongValue(
                    MockEntityClassDefine.ORDER_CLASS.field("订单用户关联").get(),
                    user.id()
                )
            )
            .build();
    }

    /**
     * 构造一个 MockEntityClassDefine.ORDER_ITEM_CLASS 实例.
     *
     * @param order 相关的 MockEntityClassDefine.ORDER_CLASS 实例.
     * @return 实例.
     */
    public IEntity buildOrderItem(IEntity order) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.ORDER_ITEM_CLASS.ref())
            .withValue(
                new StringValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("物品名称").get(),
                    faker.food().fruit()
                )
            )
            .withValue(
                new DecimalValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("金额").get(),
                    new BigDecimal(faker.number().randomDouble(3, 1, 1000))
                        .setScale(6, BigDecimal.ROUND_HALF_UP)
                )
            )
            .withValue(
                new LookupValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("单号lookup").get(),
                    order.id()
                )
            )
            .withValue(
                new LongValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                    order.id()
                )
            )
            .withValue(
                new LongValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("数量").get(),
                    faker.number().randomNumber()
                )
            )
            .withValue(
                new LongValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("订单项订单关联").get(),
                    order.id()
                )
            )
            .withValue(
                new DateTimeValue(
                    MockEntityClassDefine.ORDER_ITEM_CLASS.field("时间").get(),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                )
            )
            .build();
    }

    /**
     * 构造一个静态 lookup 动态的静态一端.
     */
    public IEntity buildOdLookupEntity(IEntity targetEntity) {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.OD_LOOKUP_ORIGINAL_ENTITY_CLASS.ref())
            .withValue(
                new LookupValue(
                    MockEntityClassDefine.OD_LOOKUP_ORIGINAL_ENTITY_CLASS.field("od-lookup-original-long").get(),
                    targetEntity.id()
                )
            ).build();
    }

    /**
     * 构造一个静态 lookup 动态的动态一端.
     */
    public IEntity buildOdLookupTargetEntity() {
        return Entity.Builder.anEntity()
            .withEntityClassRef(MockEntityClassDefine.OD_LOOKUP_TARGET_ENTITY_CLASS.ref())
            .withValue(
                new LongValue(
                    MockEntityClassDefine.OD_LOOKUP_TARGET_ENTITY_CLASS.field("od-lookup-target-long").get(),
                    faker.number().numberBetween(100, 10000)
                )
            ).build();
    }


}
