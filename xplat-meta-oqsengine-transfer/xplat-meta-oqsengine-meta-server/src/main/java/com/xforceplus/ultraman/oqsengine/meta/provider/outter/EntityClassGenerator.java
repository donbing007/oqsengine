package com.xforceplus.ultraman.oqsengine.meta.provider.outter;

import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;

/**
 * provider by outer service.
 *
 * @author xujia
 * @since 1.8
 */
public interface EntityClassGenerator {

    ServerSyncEvent pull(String appId, String env);
}
