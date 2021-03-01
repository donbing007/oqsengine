package com.xforceplus.ultraman.oqsengine.meta.mock;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;

import static com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassSyncRspProtoBuilder.entityClassSyncRspProtoGenerator;

/**
 * desc :
 * name : EntityClassGenerator
 *
 * @author : xujia
 * date : 2021/3/1
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
        return new AppUpdateEvent("mock", appId, env, version, entityClassSyncRspProtoGenerator(entityId));
    }
}
