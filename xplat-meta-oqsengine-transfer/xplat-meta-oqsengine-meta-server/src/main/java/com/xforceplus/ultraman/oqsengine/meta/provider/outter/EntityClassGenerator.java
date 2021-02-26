package com.xforceplus.ultraman.oqsengine.meta.provider.outter;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;

/**
 * desc :
 * name : EntityClassGenerator
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface EntityClassGenerator {

    AppUpdateEvent pull(String appId, String env);
}
