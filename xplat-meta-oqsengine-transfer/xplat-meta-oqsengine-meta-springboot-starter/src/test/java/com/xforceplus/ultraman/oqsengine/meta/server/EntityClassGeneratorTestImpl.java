package com.xforceplus.ultraman.oqsengine.meta.server;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;
import org.springframework.stereotype.Component;

/**
 * desc :
 * name : EntityClassGeneratorTestImpl
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@Component
public class EntityClassGeneratorTestImpl implements EntityClassGenerator {
    @Override
    public AppUpdateEvent pull(String appId, String env) {
        return new AppUpdateEvent("mock", appId, env, 1, EntityClassSyncRspProto.newBuilder().build());
    }
}
