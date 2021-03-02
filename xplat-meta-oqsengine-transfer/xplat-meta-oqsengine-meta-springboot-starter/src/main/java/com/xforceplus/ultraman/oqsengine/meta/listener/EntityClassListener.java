package com.xforceplus.ultraman.oqsengine.meta.listener;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : EntityClassListener
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
@Component
public class EntityClassListener {

    private Logger logger = LoggerFactory.getLogger(EntityClassListener.class);

    @Resource
    private SyncResponseHandler responseHandler;

    @EventListener
    public boolean publish(AppUpdateEvent event) {

        if (null == event) {
            logger.warn("event shouldNot be null, event will ignore...");
            return false;
        }

        if (null == event.getAppId() ||
                null == event.getEnv() ||
                NOT_EXIST_VERSION >= event.getVersion() ||
                null == event.getEntityClassSyncRspProto()) {
            logger.warn("appId/env/version/data shouldNot be null, event {} will ignore...", event.toString());
            return false;
        }

        return responseHandler.push(event);
    }
}
