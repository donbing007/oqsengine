package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public interface ServerSyncEvent {

    /**
     * 获取更新内容.
     * @return
     */
    public EntityClassSyncRspProto entityClassSyncRspProto();

    /**
     * 更新的AppId.
     * @return
     */
    public String appId();

    /**
     * 更新的Env.
     * @return
     */
    public String env();

    /**
     * 更新的version.
     * @return
     */
    public int version();
}
