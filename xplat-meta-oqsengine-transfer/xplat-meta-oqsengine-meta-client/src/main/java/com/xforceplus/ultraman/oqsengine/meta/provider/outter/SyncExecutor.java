package com.xforceplus.ultraman.oqsengine.meta.provider.outter;


import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;

/**
 * interface, provider to outer.
 *
 * @author xujia
 * @since 1.8
 */
public interface SyncExecutor {

    /**
     * 外部实现sync接口.
     */
    boolean sync(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto);

    /**
     * 外部实现获取版本.
     */
    int version(String appId);
}
