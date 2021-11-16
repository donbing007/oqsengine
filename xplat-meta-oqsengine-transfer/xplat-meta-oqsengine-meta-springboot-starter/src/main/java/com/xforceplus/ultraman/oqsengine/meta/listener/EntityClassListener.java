package com.xforceplus.ultraman.oqsengine.meta.listener;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.meta.handler.IResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.listener.dto.AppUpdateEvent;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;


/**
 * 帧听器.
 *
 * @author xujia
 * @since 1.8
 */
public class EntityClassListener implements ApplicationListener<AppUpdateEvent> {

    private final Logger logger = LoggerFactory.getLogger(EntityClassListener.class);

    @Resource
    private IResponseHandler responseHandler;

    @Override
    public void onApplicationEvent(AppUpdateEvent event) {

        if (event.appId().isEmpty()
            || event.env().isEmpty()
            || NOT_EXIST_VERSION >= event.version()
            || !event.entityClassSyncRspProto().isInitialized()) {
            logger.warn("appId/env/version/data shouldNot be null, event [{}-{}-{}-{}] will ignore...",
                event.appId().isEmpty() ? "empty-appId" : event.appId(),
                event.env().isEmpty() ? "empty-env" : event.env(),
                NOT_EXIST_VERSION >= event.version() ? "invalid-version" : event.version(),
                event.entityClassSyncRspProto().isInitialized());
            return;
        }

        responseHandler.push(event);
    }
}
