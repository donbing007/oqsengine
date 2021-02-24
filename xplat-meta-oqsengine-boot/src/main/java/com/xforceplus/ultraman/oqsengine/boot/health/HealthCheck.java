package com.xforceplus.ultraman.oqsengine.boot.health;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/4/1 15:17
 * @since 1.8
 */
@Order(Integer.MAX_VALUE)
@Component
public class HealthCheck implements HealthIndicator {

    final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    private IEntityField notExistField =
        EntityField.Builder.anEntityField().withId(1).withName("test").withFieldType(FieldType.STRING).build();
    private IEntityClass notExistClass =
        EntityClass.Builder.anEntityClass().withId(1).withCode("test").withField(notExistField).build();
    private IValue notExistValue = new StringValue(notExistField, "test");

    @Override
    public Health health() {

        try {
            entitySearchService.selectOne(1, notExistClass);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
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
            entitySearchService.selectByConditions(conditions, notExistClass, Page.newSinglePage(1));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Health.down(e).build();
        }

        try {
            commitIdStatusService.getMin();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }

        return Health.up().build();
    }
}
