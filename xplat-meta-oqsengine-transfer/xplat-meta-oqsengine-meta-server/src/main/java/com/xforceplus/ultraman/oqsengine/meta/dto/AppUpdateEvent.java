package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
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
    private int version;
    private EntityClassSyncRspProto entityClassSyncRspProto;

    public AppUpdateEvent(Object source, String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        super(source);
        this.appId = appId;
        this.version = version;
        this.entityClassSyncRspProto = entityClassSyncRspProto;
    }

    public EntityClassSyncRspProto getEntityClassSyncRspProto() {
        return entityClassSyncRspProto;
    }

    public String getAppId() {
        return appId;
    }

    public int getVersion() {
        return version;
    }
}
