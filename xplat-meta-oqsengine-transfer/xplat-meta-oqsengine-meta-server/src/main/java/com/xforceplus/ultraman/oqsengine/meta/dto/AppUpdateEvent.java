package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import org.springframework.context.ApplicationEvent;

/**
 * desc :
 * name : AppUpdateEvent
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class AppUpdateEvent extends ApplicationEvent {
    private String appId;
    private String env;
    private int version;
    /**
     * standard
     */
    private EntityClassSyncRspProto entityClassSyncRspProto;

    /**
     * extend
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
