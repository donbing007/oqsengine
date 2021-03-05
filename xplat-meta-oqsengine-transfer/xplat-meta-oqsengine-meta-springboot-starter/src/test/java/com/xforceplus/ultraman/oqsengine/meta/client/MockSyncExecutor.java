package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageBuilderUtils.protoToStorageList;

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

        Assert.assertNotNull(entityClassSyncRspProto);

        List<EntityClassStorage> entityClassStorageList = protoToStorageList(entityClassSyncRspProto);
        return true;
    }

    @Override
    public int version(String appId) {
        return 0;
    }
}
