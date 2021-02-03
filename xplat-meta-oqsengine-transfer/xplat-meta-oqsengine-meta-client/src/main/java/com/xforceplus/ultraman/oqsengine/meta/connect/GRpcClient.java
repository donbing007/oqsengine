package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;

/**
 * desc :
 * name : GRpcClient
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public interface GRpcClient {
    void create();

    void destroy();

    boolean opened();

    EntityClassSyncGrpc.EntityClassSyncStub channelStub();
}
