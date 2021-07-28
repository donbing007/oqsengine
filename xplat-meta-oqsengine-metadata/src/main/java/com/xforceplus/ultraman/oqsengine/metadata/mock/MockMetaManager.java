package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个MetaManager的mock实现,用以集成测试等不需要实际依赖元信息服务.
 *
 * @author dongbin
 * @version 0.1 2021/04/14 15:49
 * @since 1.8
 */
public class MockMetaManager implements MetaManager {

    private Map<String, IEntityClass> entityClassPool;
    private Map<String, IEntityClass> profileEntityClassPool;

    public MockMetaManager() {
        entityClassPool = new ConcurrentHashMap<>();
        profileEntityClassPool = new ConcurrentHashMap<>();
    }

    /**
     * 增加元信息.
     */
    public void addEntityClass(IEntityClass entityClass) {
        entityClass.family().stream().forEach(e -> {
            entityClassPool.put(buildKey(e.id(), e.version()), e);
        });
    }

    /**
     * 增加元信息.
     */
    public void addEntityClass(IEntityClass entityClass, String profile) {
        if (profile == null) {
            addEntityClass(entityClass);
        } else {
            entityClass.family().stream().forEach(e -> {
                entityClassPool.put(buildKey(e.id(), e.version(), profile), e);
            });
        }
    }

    @Override
    public Optional<IEntityClass> load(long id) {
        /*
         * 找出所有版本中版本最大的.
         */
        return Optional.ofNullable(entityClassPool.entrySet().stream().filter(e -> e.getValue().id() == id)
            .max((e0, e1) -> {
                if (e0.getValue().version() < e1.getValue().version()) {
                    return -1;
                } else if (e0.getValue().version() > e1.getValue().version()) {
                    return 1;
                } else {
                    return 0;
                }
            }).get().getValue());
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {
        if (profile == null || profile.isEmpty()) {
            return load(id);
        } else {
            return Optional.ofNullable(profileEntityClassPool.entrySet().stream().filter(e -> e.getValue().id() == id)
                .max((e0, e1) -> {
                    if (e0.getValue().version() < e1.getValue().version()) {
                        return -1;
                    } else if (e0.getValue().version() > e1.getValue().version()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }).get().getValue());
        }

    }

    @Override
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.ofNullable(entityClassPool.get(buildKey(id, version)));
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public void invalidateLocal() {
        entityClassPool.clear();
        profileEntityClassPool.clear();
    }

    @Override
    public boolean dataImport(String appId, String env, int version, String content) {
        try {
            EntityClassStorageHelper.toEntityClassSyncRspProto(content);
            return true;
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
    }

    @Override
    public Optional<MetaMetrics> showMeta(String appId) {
        return Optional.empty();
    }

    private String buildKey(long id, int version) {
        return buildKey(id, version, null);
    }

    private String buildKey(long id, int version, String profile) {
        if (profile != null) {
            return String.format("%d-%d-%s", id, version, profile);
        } else {
            return String.format("%d-%d", id, version);
        }
    }
}
