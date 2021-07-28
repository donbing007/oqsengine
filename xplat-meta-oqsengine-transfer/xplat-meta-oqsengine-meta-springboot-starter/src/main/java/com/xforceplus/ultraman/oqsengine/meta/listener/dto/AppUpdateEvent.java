package com.xforceplus.ultraman.oqsengine.meta.listener.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;
import org.springframework.context.ApplicationEvent;

/**
 * event.
 *
 * @author xujia
 * @since 1.8
 */
public class AppUpdateEvent extends ApplicationEvent implements ServerSyncEvent {

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
