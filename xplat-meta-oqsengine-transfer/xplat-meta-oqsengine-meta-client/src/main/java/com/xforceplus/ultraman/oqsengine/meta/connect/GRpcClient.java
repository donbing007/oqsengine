package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;

/**
 * grpc client interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface GRpcClient extends IBasicSyncExecutor {
    boolean opened();

    EntityClassSyncGrpc.EntityClassSyncStub channelStub();
}
