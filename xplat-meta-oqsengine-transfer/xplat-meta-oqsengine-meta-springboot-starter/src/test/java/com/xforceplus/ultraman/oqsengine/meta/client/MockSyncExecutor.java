package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.springframework.stereotype.Component;

/**
 * desc :
 * name : MockSyncExecutor
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@Component
public class MockSyncExecutor implements SyncExecutor {
    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        return false;
    }

    @Override
    public int version(String appId) {
        return 0;
    }
}
