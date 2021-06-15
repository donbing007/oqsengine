package com.xforceplus.ultraman.oqsengine.metadata.integration;

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
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        try {
            return super.sync(appId, version, entityClassSyncRspProto);
        } finally {
            syncedEntityClassMaps.putIfAbsent(appId + "_" + version, entityClassSyncRspProto.getEntityClassesList());
        }
    }

    public List<EntityClassInfo> getEntityClasses(String appId, int version) {
        return syncedEntityClassMaps.get(appId + "_" + version);
    }

    public void clear() {
        syncedEntityClassMaps.clear();
    }
}
