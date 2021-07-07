package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class MockMetaManagerHolder {

    /**
     *  add entityClasses.
     */
    public static void initEntityClassBuilder(List<IEntityClass> entityClasses) throws IllegalAccessException {
        if (null != entityClasses) {
            for (IEntityClass entityClass : entityClasses) {
                ((MockMetaManager) MetaInitialization.getInstance().getMetaManager()).addEntityClass(entityClass);
            }
        }
    }

    /**
     *  reset MetaManager.
     */
    public static void resetMetaManager(IRequestHandler requestHandler) throws IllegalAccessException {
        if (null == requestHandler) {
            MetaInitialization.getInstance().setMetaManager(new MockMetaManager());
        } else {
            MetaManager metaManager = new StorageMetaManager();

            Collection<Field> cacheFields = ReflectionUtils.printAllMembers(metaManager);
            ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", metaManager,
                MetaInitialization.getInstance().getCacheExecutor());
            ReflectionUtils.reflectionFieldValue(cacheFields, "requestHandler", metaManager, requestHandler);
            ReflectionUtils.reflectionFieldValue(cacheFields, "asyncDispatcher", metaManager,
                CommonInitialization.getInstance().getRunner());

            MetaInitialization.getInstance().setMetaManager(metaManager);
        }
    }
}
