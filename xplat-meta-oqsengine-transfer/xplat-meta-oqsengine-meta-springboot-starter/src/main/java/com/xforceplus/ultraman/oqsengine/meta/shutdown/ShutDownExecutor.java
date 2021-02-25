package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * desc :
 * name : ShutDownExecutor
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
public class ShutDownExecutor {

    @Resource
    private IShutDown shutDown;

    @PreDestroy
    public void shutDown() {
        shutDown.shutdown();
    }
}
