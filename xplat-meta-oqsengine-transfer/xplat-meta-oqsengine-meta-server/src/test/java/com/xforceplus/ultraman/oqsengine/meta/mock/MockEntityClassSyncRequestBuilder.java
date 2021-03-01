package com.xforceplus.ultraman.oqsengine.meta.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandlerTest;

/**
 * desc :
 * name : MockEntityClassSyncRequestBuilder
 *
 * @author : xujia
 * date : 2021/3/1
 * @since : 1.8
 */
public class MockEntityClassSyncRequestBuilder {

    public static EntityClassSyncRequest entityClassSyncRequest(SyncResponseHandlerTest.Case requestCase) {
        return EntityClassSyncRequest.newBuilder().setUid(requestCase.getUid())
                .setEnv(requestCase.getEnv())
                .setAppId(requestCase.getAppId())
                .setVersion(requestCase.getVersion())
                .setStatus(requestCase.getStatus())
                .build();
    }
}
