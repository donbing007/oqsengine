package com.xforceplus.ultraman.oqsengine.meta.listener;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : EntityClassListener
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public class EntityClassListener {

    private Logger logger = LoggerFactory.getLogger(EntityClassListener.class);

    @Resource
    private SyncResponseHandler responseHandler;

    @EventListener
    public boolean appSyncListener(AppUpdateEvent event) {
        if (null == event) {
            logger.warn("event should not be null, event will ignore...");
            return false;
        }
        if (null == event.getAppId() ||
                null == event.getEnv() ||
                NOT_EXIST_VERSION >= event.getVersion() ||
                null == event.getEntityClassSyncRspProto()) {
            logger.warn("appId/env/version/data should not be null, event will ignore...");
            return false;
        }

        return responseHandler.push(event);
    }
}
