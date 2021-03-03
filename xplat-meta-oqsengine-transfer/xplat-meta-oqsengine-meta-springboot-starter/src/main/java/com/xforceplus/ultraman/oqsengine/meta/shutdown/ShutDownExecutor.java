package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private IShutDown shutDown;

    @PreDestroy
    public void shutDown() {
        shutDown.shutdown();
    }
}
