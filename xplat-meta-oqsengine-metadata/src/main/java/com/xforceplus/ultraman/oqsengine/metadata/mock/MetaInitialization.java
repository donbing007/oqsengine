package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.handler.DefaultEntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.handler.EntityClassFormatHandler;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class MetaInitialization implements BeanInitialization {

    private static volatile MetaInitialization instance = null;

    public DefaultCacheExecutor cacheExecutor;

    public EntityClassSyncExecutor entityClassSyncExecutor;

    public EntityClassFormatHandler entityClassFormatHandler;

    public MetaManager metaManager;

    private MetaInitialization() {
    }

    /**
     * 获取单例.
     */
    public static MetaInitialization getInstance() throws IllegalAccessException {
        if (null == instance) {
            synchronized (MetaInitialization.class) {
                if (null == instance) {
                    instance = new MetaInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws IllegalAccessException {
        cacheExecutor = new DefaultCacheExecutor();

        Collection<Field> fields = ReflectionUtils.printAllMembers(cacheExecutor);
        ReflectionUtils.reflectionFieldValue(fields, "redisClient", cacheExecutor,
            CommonInitialization.getInstance().getRedisClient());
        cacheExecutor.init();

        entityClassFormatHandler = new DefaultEntityClassFormatHandler();
        Collection<Field> classLoaderFields = ReflectionUtils.printAllMembers(entityClassFormatHandler);
        ReflectionUtils.reflectionFieldValue(classLoaderFields, "cacheExecutor", entityClassFormatHandler,
            MetaInitialization.getInstance().getCacheExecutor());

        entityClassSyncExecutor = new EntityClassSyncExecutor();

        Collection<Field> cacheFields = ReflectionUtils.printAllMembers(entityClassSyncExecutor);
        ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", entityClassSyncExecutor, cacheExecutor);
        ReflectionUtils.reflectionFieldValue(cacheFields, "expireExecutor", entityClassSyncExecutor, new ExpireExecutor());
        ReflectionUtils.reflectionFieldValue(cacheFields, "eventBus", entityClassSyncExecutor, new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                if (!(type.equals(EventType.AUTO_FILL_UPGRADE) || type.equals(EventType.AGGREGATION_TREE_UPGRADE))) {
                    throw new IllegalArgumentException(
                        String.format("type %s not equals to %s", type, EventType.AUTO_FILL_UPGRADE + "or" + EventType.AGGREGATION_TREE_UPGRADE));
                }
            }

            @Override
            public void notify(Event event) {
                if (!(event.type().equals(EventType.AUTO_FILL_UPGRADE) || event.type().equals(EventType.AGGREGATION_TREE_UPGRADE))) {
                    throw new IllegalArgumentException(
                        String.format("type %s not equals to %s", event.type(), EventType.AUTO_FILL_UPGRADE + "or" + EventType.AGGREGATION_TREE_UPGRADE));
                }
            }
        });

        entityClassSyncExecutor.start();

        metaManager = new MockMetaManager();

    }

    @Override
    public void clear() throws Exception {
        if (null != metaManager) {
            metaManager.invalidateLocal();
        }
    }

    @Override
    public void destroy() throws Exception {
        cacheExecutor.destroy();

        entityClassSyncExecutor.stop();

        metaManager = null;

        instance = null;
    }

    public DefaultCacheExecutor getCacheExecutor() {
        return cacheExecutor;
    }

    public EntityClassSyncExecutor getEntityClassSyncExecutor() {
        return entityClassSyncExecutor;
    }

    public EntityClassFormatHandler getEntityClassFormatHandler() {
        return entityClassFormatHandler;
    }

    public void setEntityClassFormatHandler(
        EntityClassFormatHandler entityClassFormatHandler) {
        this.entityClassFormatHandler = entityClassFormatHandler;
    }

    public MetaManager getMetaManager() {
        return metaManager;
    }

    public void setMetaManager(MetaManager metaManager) {
        this.metaManager = metaManager;
    }
}
