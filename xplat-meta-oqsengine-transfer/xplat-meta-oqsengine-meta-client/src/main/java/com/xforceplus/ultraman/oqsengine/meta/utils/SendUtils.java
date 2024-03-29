package com.xforceplus.ultraman.oqsengine.meta.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interface, provider to outer.
 *
 * @author xujia
 * @since 1.8
 */
public class SendUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendUtils.class);

    /**
     * 响应response处理结果，需要进行check-requestWatcher的可用性.
     */
    public static void sendRequest(RequestWatcher requestWatcher,
                                                 EntityClassSyncRequest entityClassSyncRequest,
                                                 boolean checkActive) {
        //  这里由于异步执行了OQS的缓存更新，等待后可能出现新的流始化了，所以必须进行doubleCheck判断uid是否相同
        if (null == requestWatcher || (checkActive && !requestWatcher.isActive())) {
            LOGGER.warn("stream observer not exists.");
            throw new MetaSyncClientException("stream observer not exists or was expired.", true);
        }
        try {
            requestWatcher.observer().onNext(entityClassSyncRequest);
            printLog(entityClassSyncRequest);
        } catch (Exception e) {
            throw new MetaSyncClientException(
                String.format("send request error, message-[%s].", e.getMessage()), true);
        }
    }

    private static void printLog(EntityClassSyncRequest entityClassSyncRequest) {
        try {
            RequestStatus requestStatus = RequestStatus.getInstance(entityClassSyncRequest.getStatus());
            if (entityClassSyncRequest.getStatus() == RequestStatus.HEARTBEAT.ordinal()) {
                LOGGER.debug("send request success, request [{}, {}, {}]",
                    "HEARTBEAT",
                    "STATUS:" + (null == requestStatus ? "UN_KNOW" : requestStatus.name()),
                    "UID:" + entityClassSyncRequest.getUid());
            } else {
                String appId = entityClassSyncRequest.getAppId();
                LOGGER.debug("send request success, request [{}, {}, {}, {}, {}]",
                    "REQ APP_ID:" + appId,
                    "ENV:" + entityClassSyncRequest.getEnv(),
                    "VER:" + entityClassSyncRequest.getVersion(),
                    "STATUS:" + (null == requestStatus ? "UN_KNOW" : requestStatus.name()),
                    "UID:" + entityClassSyncRequest.getUid());
            }

        } catch (Exception e) {
            //ignore
        }
    }
}
