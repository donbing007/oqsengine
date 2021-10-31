package com.xforceplus.ultraman.oqsengine.meta.provider.outter;


import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;

/**
 * desc :
 * name : SyncExecutor
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public interface SyncExecutor {
    /**
     * 外部实现sync接口.
     */
    boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto);

    boolean dataImport(String appId, int version, String content);

    /**
     * 外部实现获取版本.
     */
    int version(String appId);
}
