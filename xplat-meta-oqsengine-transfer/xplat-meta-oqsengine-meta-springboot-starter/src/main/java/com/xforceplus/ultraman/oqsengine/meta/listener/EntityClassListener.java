package com.xforceplus.ultraman.oqsengine.meta.listener;

import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.handler.IResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

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
public class EntityClassListener implements ApplicationListener<AppUpdateEvent> {

    private Logger logger = LoggerFactory.getLogger(EntityClassListener.class);

    @Resource
    private IResponseHandler responseHandler;

    @Override
    public void onApplicationEvent(AppUpdateEvent event) {

        if (null == event.getAppId() ||
                null == event.getEnv() ||
                NOT_EXIST_VERSION >= event.getVersion() ||
                null == event.getEntityClassSyncRspProto()) {
            logger.warn("appId/env/version/data shouldNot be null, event {} will ignore...", event.toString());
            return;
        }

        responseHandler.push(event);
    }
}
