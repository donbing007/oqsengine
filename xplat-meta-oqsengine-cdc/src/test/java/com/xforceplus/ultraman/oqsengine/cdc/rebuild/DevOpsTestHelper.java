package com.xforceplus.ultraman.oqsengine.cdc.rebuild;


import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.List;

/**
 * 抽像的devops启动容器.
 *
 * @author xujia 2020/11/22
 * @since 1.8
 */
public abstract class DevOpsTestHelper extends AbstractCDCTestHelper {

    public void init(List<IEntityClass> entityClasses) throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(entityClasses);
        consumerRunner = initConsumerRunner();
        consumerRunner.start();
    }

    public void clear() throws Exception {
        InitializationHelper.clearAll();
    }
}
