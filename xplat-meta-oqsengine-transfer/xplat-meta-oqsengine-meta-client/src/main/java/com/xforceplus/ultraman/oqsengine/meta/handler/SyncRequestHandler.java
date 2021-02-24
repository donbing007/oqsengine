package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;

/**
 * desc :
 * name : EntityClassExecutorService
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public class SyncRequestHandler implements IRequestHandler {

    private Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

    @Resource
    private SyncExecutor syncExecutor;

    @Resource
    private RequestWatchExecutor requestWatchExecutor;


    @Override
    public boolean register(String appId, int version) {
        return register(Collections.singletonList(new AbstractMap.SimpleEntry<>(appId, version)));
    }

    @Override
    public synchronized boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries) {
        RequestWatcher watcher = requestWatchExecutor.watcher();
        /**
         * 这里只判断是否watcher为空，如果服务watcher不为空
         */
        if (null == watcher) {
            logger.warn("current gRpc-client is not init, can't offer appIds:{}."
                    , appIdEntries.stream().map(AbstractMap.SimpleEntry::getKey).collect(Collectors.toList()));
            appIdEntries.forEach(
                    a -> {
                        requestWatchExecutor.addForgot(a.getKey(), a.getValue());
                    }
            );

            return false;
        }

        appIdEntries.stream()
                .filter(s -> {
                    if (watcher.watches().containsKey(s.getKey())) {
                        logger.info("appId : {} is already in watchList, will ignore...", s.getKey());
                        return false;
                    } else {
                        logger.info("add appId : {} in watchList", s.getKey());
                        return true;
                    }
                })
                .forEach(
                        v -> {
                            /**
                             * 当前requestWatch不可用或发生UID切换时,先加入forgot列表
                             */
                            if (!requestWatchExecutor.canAccess(watcher.uid())) {
                                requestWatchExecutor.addForgot(v.getKey(), v.getValue());
                            } else {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setUid(watcher.uid()).setAppId(v.getKey()).setVersion(v.getValue())
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                WatchElement.AppStatus status = WatchElement.AppStatus.Register;

                                try {
                                    sendRequest(requestWatchExecutor.watcher(), entityClassSyncRequest,
                                            requestWatchExecutor.accessFunction(), entityClassSyncRequest.getUid());
                                } catch (Exception e) {
                                    status = WatchElement.AppStatus.Init;
                                }

                                requestWatchExecutor.add(new WatchElement(v.getKey(), v.getValue(), status));
                            }
                        }
                );

        logger.info("current watchList status : {}", watcher.watches().toString());

        return true;
    }

    /**
     * 当发生断线时，需要重新连接，每次重新连接后，需要将当前OQS的WatchList重新注册到元数据中
     */
    public boolean reRegister() {
        /**
         * 当开启reRegister操作时，所有的register操作将被中断
         */
        RequestWatcher requestWatcher = requestWatchExecutor.watcher();
        boolean isOperationOK = true;
        if (null != requestWatcher && requestWatcher.watches().size() > 0) {
            for (Map.Entry<String, WatchElement> e : requestWatcher.watches().entrySet()) {
                try {
                    EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                    EntityClassSyncRequest entityClassSyncRequest =
                            builder.setAppId(e.getKey())
                                    .setVersion(version(e.getKey()))
                                    .setUid(requestWatcher.uid())
                                    .setStatus(RequestStatus.REGISTER.ordinal()).build();

                    sendRequest(requestWatcher, entityClassSyncRequest);
                } catch (Exception ex) {
                    isOperationOK = false;
                    logger.warn("reRegister watcherElement-[{}] failed.", e.getValue().toString());
                    requestWatcher.observer().onError(ex);
                    break;
                }
            }
        }
        return isOperationOK;
    }

    @Override
    public void accept(EntityClassSyncResponse entityClassSyncResponse) {
        /**
         * 执行OQS更新EntityClass
         */
        EntityClassSyncRequest.Builder entityClassSyncRequestBuilder = execute(entityClassSyncResponse);
        /**
         * 回写处理结果, entityClassSyncRequest为空则代表传输存在问题.
         */
        try {
            sendRequest(requestWatchExecutor.watcher(), entityClassSyncRequestBuilder.setUid(entityClassSyncResponse.getUid()).build(),
                    requestWatchExecutor.accessFunction(), entityClassSyncResponse.getUid());
        } catch (Exception ex) {
            throw new MetaSyncClientException(
                    String.format("stream observer ack error, message-[%s].", ex.getMessage()), true);
        }
    }

    /**
     * 执行方法
     *
     * @param entityClassSyncResponse
     * @return EntityClassSyncRequest
     */
    @SuppressWarnings("unchecked")
    private EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse) {

        try {
            /**
             * 该方法返回的错误不会导致重新连接、但会通知服务端本次推送更新失败
             */
            int status = SYNC_FAIL.ordinal();
            EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
            try {
                /**
                 * md5 check && 是否已存在版本判断
                 */
                EntityClassSyncRspProto result = entityClassSyncResponse.getEntityClassSyncRspProto();
                if (md5Check(entityClassSyncResponse.getMd5(), result)) {

                    int oqsVersion = version(entityClassSyncResponse.getAppId());

                    if (oqsVersion < entityClassSyncResponse.getVersion()) {
                        /**
                         * 执行外部传入的执行器
                         */
                        status = syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(), result) ?
                                RequestStatus.SYNC_OK.ordinal() : SYNC_FAIL.ordinal();

                    } else {
                        logger.warn("current oqs-version {} bigger than sync-version : {}, will ignore...",
                                oqsVersion, entityClassSyncResponse.getVersion());
                        status = RequestStatus.SYNC_OK.ordinal();
                    }
                    return builder.setStatus(status)
                            .setAppId(entityClassSyncResponse.getAppId())
                            .setVersion(entityClassSyncResponse.getVersion());
                }

            } catch (Exception e) {
                logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
            }

            return builder.setStatus(SYNC_FAIL.ordinal());
        } catch (Exception e) {
            logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
            return EntityClassSyncRequest.newBuilder().setStatus(SYNC_FAIL.ordinal());
        }
    }

    private boolean md5Check(String md5, EntityClassSyncRspProto entityClassSyncRspProto) {
        if (null == md5 || md5.isEmpty() || null == entityClassSyncRspProto) {
            return false;
        }
        return md5.equals(getMD5(entityClassSyncRspProto.toByteArray()));
    }

    private int version(String appId) {
        return syncExecutor.version(appId);
    }
}
