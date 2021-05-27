package com.xforceplus.ultraman.oqsengine.meta.provider.outter;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;

/**
 * provider by outer service.
 *
 * @author xujia
 * @since 1.8
 */
public interface EntityClassGenerator {

    AppUpdateEvent pull(String appId, String env);
}
