package com.xforceplus.ultraman.oqsengine.metadata.recover.server.mock;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;


/**
 * desc :.
 * name : EntityClassGenerator
 *
 * @author : xujia 2021/3/1
 * @since : 1.8
 */
public class MockEntityClassGenerator implements EntityClassGenerator {

    private int version;
    private long entityId;

    public void reset(int version, long entityId) {
        this.version = version;
        this.entityId = entityId;
    }

    @Override
    public AppUpdateEvent pull(String appId, String env) {
        return new AppUpdateEvent("mock", appId, env, version,
            MockEntityClassSyncRspProtoBuilder.entityClassSyncRspProtoGenerator(entityId));
    }

}
