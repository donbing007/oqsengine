package com.xforceplus.ultraman.oqsengine.meta.server;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.listener.dto.AppUpdateEvent;
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
    public static int version = 2;

    @Override
    public AppUpdateEvent pull(String appId, String env) {
        return new AppUpdateEvent("mock", appId, env, version, EntityClassSyncRspProto.newBuilder().build());
    }
}
