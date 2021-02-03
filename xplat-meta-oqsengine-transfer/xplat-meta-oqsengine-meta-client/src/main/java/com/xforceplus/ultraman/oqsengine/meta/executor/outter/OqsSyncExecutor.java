package com.xforceplus.ultraman.oqsengine.meta.executor.outter;


import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;

/**
 * desc :
 * name : SyncExecutor
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public interface OqsSyncExecutor {
    boolean sync(EntityClassSyncRspProto entityClassSyncRspProto);

    int version(String appId);
}
