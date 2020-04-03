package com.xforceplus.ultraman.oqsengine.boot.health;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author dongbin
 * @version 0.1 2020/4/1 15:17
 * @since 1.8
 */
@Order(Integer.MAX_VALUE)
@Component
public class HealthCheck implements HealthIndicator {

    @Resource
    private EntitySearchService entitySearchService;

    private IEntityField notExistField = new Field(1, "test", FieldType.STRING);
    private IEntityClass notExistClass = new EntityClass(1, "test", Arrays.asList(notExistField));
    private IValue notExistValue = new StringValue(notExistField, "test");

    @Override
    public Health health() {

        try {
            entitySearchService.selectOne(1, notExistClass);
        } catch (SQLException e) {
            return Health.down(e).build();
        }


        Conditions conditions = Conditions.buildEmtpyConditions().addAnd(
            new Condition(
                notExistField,
                ConditionOperator.EQUALS,
                notExistValue
            )
        );

        try {
            entitySearchService.selectByConditions(conditions, notExistClass, Page.newSinglePage(100));
        } catch (SQLException e) {
            return Health.down(e).build();
        }


        return Health.up().build();
    }
}
