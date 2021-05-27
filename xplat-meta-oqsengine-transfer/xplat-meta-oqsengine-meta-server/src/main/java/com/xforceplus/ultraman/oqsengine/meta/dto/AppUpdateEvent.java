package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import org.springframework.context.ApplicationEvent;

/**
 * event.
 *
 * @author xujia
 * @since 1.8
 */
public class AppUpdateEvent extends ApplicationEvent {
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


    public AppUpdateEvent(Object source, String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        super(source);
        this.appId = appId;
        this.env = env;
        this.version = version;
        this.entityClassSyncRspProto = entityClassSyncRspProto;
    }

    public EntityClassSyncRspProto getEntityClassSyncRspProto() {
        return entityClassSyncRspProto;
    }

    public String getAppId() {
        return appId;
    }

    public String getEnv() {
        return env;
    }

    public int getVersion() {
        return version;
    }
}
