package com.xforceplus.ultraman.oqsengine.meta.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class MockSyncEvent implements ServerSyncEvent {

    private final String appId;
    private final String env;
    private final int version;

    /**
     * standard.
     */
    private final EntityClassSyncRspProto entityClassSyncRspProto;

    /**
     * extend.
     */
    //  todo not implements now


    public MockSyncEvent(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        this.appId = appId;
        this.env = env;
        this.version = version;
        this.entityClassSyncRspProto = entityClassSyncRspProto;
    }

    @Override
    public EntityClassSyncRspProto entityClassSyncRspProto() {
        return entityClassSyncRspProto;
    }

    @Override
    public String appId() {
        return appId;
    }

    @Override
    public String env() {
        return env;
    }

    @Override
    public int version() {
        return version;
    }
}
