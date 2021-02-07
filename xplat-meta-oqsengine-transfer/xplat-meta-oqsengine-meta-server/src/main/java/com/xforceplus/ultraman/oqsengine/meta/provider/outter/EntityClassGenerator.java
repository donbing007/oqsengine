package com.xforceplus.ultraman.oqsengine.meta.provider.outter;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;

/**
 * desc :
 * name : EntityClassGenerator
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface EntityClassGenerator {

    EntityClassSyncRspProto pull(String appId, int version);
}
