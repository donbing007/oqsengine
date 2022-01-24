package com.xforceplus.ultraman.oqsengine.boot.health;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
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
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Resource
    private MetaManager metaManager;

    private EntityClassRef entityClassRef = HealthCheckEntityClass.getInstance().ref();

    private Conditions conditions = Conditions.buildEmtpyConditions();

    private SelectConfig config = SelectConfig.Builder.anSelectConfig()
        .withSort(Sort.buildOutOfSort()).withPage(Page.emptyPage()).build();

    @Override
    public Health health() {

        try {
            masterStorage.exist(0);

            IEntityClass entityClass = metaManager.load(entityClassRef).get();

            indexStorage.select(conditions, entityClass, config);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return Health.down(ex).build();
        }

        return Health.up().build();
    }
}
