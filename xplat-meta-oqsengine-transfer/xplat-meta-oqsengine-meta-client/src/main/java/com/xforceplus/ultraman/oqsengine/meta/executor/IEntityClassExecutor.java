package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;

/**
 * desc :
 * name : EntityClassExecutor
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public interface IEntityClassExecutor {
    EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse);

    int version(String appId);
}
