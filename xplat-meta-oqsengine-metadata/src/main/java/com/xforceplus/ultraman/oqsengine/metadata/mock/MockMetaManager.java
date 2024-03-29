package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.metadata.utils.offline.OffLineMetaHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Collection;
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

    /**
     * 加载元信息.
     *
     * @param id 元信息id.
     * @return 元信息.
     */
    public Optional<IEntityClass> load(long id) {
        /*
         * 找出所有版本中版本最大的.
         */
        Optional<Map.Entry<String, IEntityClass>> op =
            entityClassPool.entrySet().stream().filter(e -> e.getValue().id() == id)
                .max((e0, e1) -> {
                    if (e0.getValue().version() < e1.getValue().version()) {
                        return -1;
                    } else if (e0.getValue().version() > e1.getValue().version()) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
        if (op.isPresent()) {
            return Optional.ofNullable(op.get().getValue());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {
        if (profile == null || profile.isEmpty()) {
            return load(id);
        } else {
            Optional<Map.Entry<String, IEntityClass>> op =
                profileEntityClassPool.entrySet().stream().filter(e -> e.getValue().id() == id)
                    .max((e0, e1) -> {
                        if (e0.getValue().version() < e1.getValue().version()) {
                            return -1;
                        } else if (e0.getValue().version() > e1.getValue().version()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    });
            if (op.isPresent()) {
                return Optional.ofNullable(op.get().getValue());
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<IEntityClass> load(long entityClassId, int version, String profile) {
        if (profile == null || profile.isEmpty()) {
            return load(entityClassId, version, profile);
        } else {
            Optional<Map.Entry<String, IEntityClass>> op =
                profileEntityClassPool.entrySet().stream().filter(e -> e.getValue().id() == entityClassId)
                    .max((e0, e1) -> {
                        if (e0.getValue().version() < e1.getValue().version()) {
                            return -1;
                        } else if (e0.getValue().version() > e1.getValue().version()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    });
            if (op.isPresent()) {
                return Optional.ofNullable(op.get().getValue());
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Collection<IEntityClass> withProfilesLoad(long entityClassId) {
        return null;
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public int need(String appId, String env, boolean overWrite) {
        return 0;
    }

    @Override
    public void invalidateLocal() {
        entityClassPool.clear();
        profileEntityClassPool.clear();
    }

    @Override
    public boolean metaImport(String appId, String env, int version, String content) {
        try {
            OffLineMetaHelper.toEntityClassSyncRspProto(content);
            return true;
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
    }

    @Override
    public Optional<MetaMetrics> showMeta(String appId) {
        return Optional.empty();
    }

    @Override
    public int reset(String appId, String env) {
        return 0;
    }

    @Override
    public boolean remove(String appId) {
        return true;
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
