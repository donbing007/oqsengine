package com.xforceplus.ultraman.oqsengine.metadata;

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

    public MockMetaManager() {
        entityClassPool = new ConcurrentHashMap<>();
    }

    /**
     * 增加元信息.
     */
    public void addEntityClass(IEntityClass entityClass) {
        entityClass.family().stream().forEach(e -> {
            entityClassPool.put(buildKey(e.id(), e.version()), e);
        });
    }

    @Override
    public Optional<IEntityClass> load(long id) {
        /*
         * 找出所有版本中版本最大的那个.
         */
        return Optional.ofNullable(entityClassPool.entrySet().stream().filter(e -> {
            return parseIdFromKey(e.getKey()) == id;
        }).max((e0, e1) -> {
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
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.ofNullable(entityClassPool.get(buildKey(id, version)));
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public void invalidateLocal() {

    }

    private String buildKey(long id, int version) {
        return String.format("%d-%d", id, version);
    }

    private long parseIdFromKey(String key) {
        String[] values = key.split("-");
        return Long.parseLong(values[0]);
    }
}
