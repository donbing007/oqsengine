package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 执行器-关闭.
 *
 * @author xujia
 * @since 1.8
 */
public class ShutDownExecutor {

    @Resource(name = "grpcShutdown")
    private IShutDown shutDown;

    @PreDestroy
    public void shutDown() {
        shutDown.shutdown();
    }
}
