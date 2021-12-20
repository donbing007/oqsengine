package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class EnhancedSyncExecutor extends EntityClassSyncExecutor {

    private final Map<String, List<EntityClassInfo>> syncedEntityClassMaps = new HashMap<>();

    /**
     * 同步appId对应的EntityClass package.
     */
    @Override
    public void sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        try {
            super.sync(appId, version, entityClassSyncRspProto);
        } catch (Exception e) {
            throw e;
        }

        syncedEntityClassMaps
            .putIfAbsent(appId + "_" + version, entityClassSyncRspProto.getEntityClassesList());
    }

    public List<EntityClassInfo> getEntityClasses(String appId, int version) {
        return syncedEntityClassMaps.get(appId + "_" + version);
    }

    public void clear() {
        syncedEntityClassMaps.clear();
    }
}
