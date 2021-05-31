package com.xforceplus.ultraman.oqsengine.meta.listener;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.handler.IResponseHandler;
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

        if (event.getAppId().isEmpty()
            || event.getEnv().isEmpty()
            || NOT_EXIST_VERSION >= event.getVersion()
            || !event.getEntityClassSyncRspProto().isInitialized()) {
            logger.warn("appId/env/version/data shouldNot be null, event [{}-{}-{}-{}] will ignore...",
                event.getAppId().isEmpty() ? "empty-appId" : event.getAppId(),
                event.getEnv().isEmpty() ? "empty-env" : event.getEnv(),
                NOT_EXIST_VERSION >= event.getVersion() ? "invalid-version" : event.getVersion(),
                event.getEntityClassSyncRspProto().isInitialized());
            return;
        }

        responseHandler.push(event);
    }
}
