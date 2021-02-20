package com.xforceplus.ultraman.oqsengine.meta.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * desc :
 * name : SendUtils
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class SendUtils {

    private static Logger logger = LoggerFactory.getLogger(SendUtils.class);

    /**
     * 响应response处理结果，需要进行check-requestWatcher的可用性
     * @param requestWatcher
     * @param entityClassSyncRequest
     * @param checkFunction
     * @param param
     */
    public static void sendRequest(RequestWatcher requestWatcher, EntityClassSyncRequest entityClassSyncRequest
                                                    , Function<String, Boolean> checkFunction, String param) {
        /**
         * 这里由于异步执行了OQS的缓存更新，等待后可能出现新的流始化了，所以必须进行doubleCheck判断uid是否相同
         */
        if (null != checkFunction) {
            if (!checkFunction.apply(param)) {
                logger.warn("stream observer not exists.");
                throw new MetaSyncClientException("stream observer not exists or was expired.", true);
            }
        }
        sendRequest(requestWatcher, entityClassSyncRequest);
    }

    /**
     * 响应response处理结果
     * @param requestWatcher
     * @param entityClassSyncRequest
     */
    public static void sendRequest(RequestWatcher requestWatcher, EntityClassSyncRequest entityClassSyncRequest) {
        try {
            requestWatcher.observer().onNext(entityClassSyncRequest);
        } catch (Exception e) {
            logger.warn("sendRequest failed.");
            throw new MetaSyncClientException("sendRequest failed.", true);
        }
    }
}
