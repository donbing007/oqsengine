package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;

/**
 * desc :
 * name : ResponseHandler
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IResponseHandler<T> {

    boolean pull(String appId, int version, String uid);

    boolean push(AppUpdateEvent event);

    void start();

    void stop();
}

