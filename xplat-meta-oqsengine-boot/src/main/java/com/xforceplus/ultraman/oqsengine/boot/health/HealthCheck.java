package com.xforceplus.ultraman.oqsengine.boot.health;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.sql.SQLException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 健康检查.
 *
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

    private EntityClassRef entityClassRef = HealthCheckEntityClass.getInstance().ref();

    private Conditions conditions = Conditions.buildEmtpyConditions();
    private ServiceSelectConfig config =
        ServiceSelectConfig.Builder.anSearchConfig().withPage(Page.emptyPage()).build();

    @Override
    public Health health() {

        try {
            entitySearchService.selectByConditions(conditions, entityClassRef, config);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Health.down(e).build();
        }

        return Health.up().build();
    }
}
