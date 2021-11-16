package com.xforceplus.ultraman.oqsengine.meta.provider.outter;


import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interface, provider to outer.
 *
 * @author xujia
 * @since 1.8
 */
public interface SyncExecutor {

    Logger LOGGER = LoggerFactory.getLogger(SyncExecutor.class);
    /**
     * 外部实现sync接口.
     */
    boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto);

    boolean dataImport(String appId, int version, String content);

    /**
     * 外部实现获取版本.
     */
    int version(String appId);

    /**
     * 记录SyncClient错误日志.
     */
    default void recordSyncFailed(String appId, Integer version, String message) {
        LOGGER.warn("sync error, appId : [{}], version : [{}], message : [{}].", appId, version, message);
    }
}
