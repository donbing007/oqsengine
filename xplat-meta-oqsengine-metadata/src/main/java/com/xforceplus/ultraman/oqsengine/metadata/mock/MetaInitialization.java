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

        entityClassSyncExecutor = new EntityClassSyncExecutor();

        Collection<Field> cacheFields = ReflectionUtils.printAllMembers(entityClassSyncExecutor);
        ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", entityClassSyncExecutor, cacheExecutor);
        ReflectionUtils.reflectionFieldValue(cacheFields, "expireExecutor", entityClassSyncExecutor, new ExpireExecutor());
        ReflectionUtils.reflectionFieldValue(cacheFields, "eventBus", entityClassSyncExecutor, new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                if (!type.equals(EventType.META_DATA_CHANGE)) {
                    throw new IllegalArgumentException(
                        String.format("type %s not equals to %s", type, EventType.META_DATA_CHANGE));
                }
            }

            @Override
            public void notify(Event event) {
                if (!event.type().equals(EventType.META_DATA_CHANGE)) {
                    throw new IllegalArgumentException(
                        String.format("type %s not equals to %s", event.type(), EventType.META_DATA_CHANGE));
                }
            }
        });

        entityClassSyncExecutor.start();

        metaManager = new MockMetaManager();

    }

    @Override
    public void clear() throws Exception {
        //  实现了clear接口、但是只清除本地缓存.
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

    public MetaManager getMetaManager() {
        return metaManager;
    }

    public void setMetaManager(MetaManager metaManager) {
        this.metaManager = metaManager;
    }
}
