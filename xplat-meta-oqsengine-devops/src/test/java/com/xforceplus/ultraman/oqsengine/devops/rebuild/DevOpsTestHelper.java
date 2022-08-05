package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 抽像的devops启动容器.
 *
 * @author xujia 2020/11/22
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class, ManticoreContainer.class})
public abstract class DevOpsTestHelper {

    public void init(List<IEntityClass> entityClasses) throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(entityClasses);
    }

    public void clear() throws Exception {
        InitializationHelper.clearAll();
    }
}
