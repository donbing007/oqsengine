package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;

/**
 * desc :
 * name : GRpcClient
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public interface GRpcClient extends IBasicSyncExecutor {
    boolean opened();

    EntityClassSyncGrpc.EntityClassSyncStub channelStub();
}
