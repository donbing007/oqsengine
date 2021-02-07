package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.EntityClassSyncResponseHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * desc :
 * name : ShutDown
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
@Component
public class ShutDown {

    @Resource
    IWatchExecutor watchExecutor;

    @Resource
    private EntityClassSyncResponseHandler responseHandler;

    @PreDestroy
    public void showdown() {
        watchExecutor.stop();

        responseHandler.stop();
    }
}
