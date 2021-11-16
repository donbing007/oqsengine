package com.xforceplus.ultraman.oqsengine.devops;


import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.util.List;

/**
 * 抽像的devops启动容器.
 *
 * @author xujia 2020/11/22
 * @since 1.8
 */
public abstract class DevOpsTestHelper extends AbstractContainerExtends {

    public void init(List<IEntityClass> entityClasses) throws IllegalAccessException {
        MockMetaManagerHolder.initEntityClassBuilder(entityClasses);
    }

    public void destroy() throws Exception {
        InitializationHelper.clearAll();
    }
}
