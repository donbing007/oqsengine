package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.executor.outter.OqsSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;

/**
 * desc :
 * name : EntityClassExecutorService
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public class EntityClassExecutorService implements EntityClassExecutor {

    private Logger logger = LoggerFactory.getLogger(EntityClassExecutorService.class);

    @Resource
    private OqsSyncExecutor oqsSyncExecutor;

    @Resource(name = "metaSyncThreadPool")
    private ExecutorService asyncDispatcher;

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    /**
     * 执行方法
     *
     * @param entityClassSyncResponse
     * @return EntityClassSyncRequest
     */
    @Override
    @SuppressWarnings("unchecked")
    public EntityClassSyncRequest.Builder execute(EntityClassSyncResponse entityClassSyncResponse) {

        CompletableFuture<EntityClassSyncRequest.Builder> future = async(() -> {
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
                        status = oqsSyncExecutor.sync(result) ?
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
        });

        future.join();

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
            return EntityClassSyncRequest.newBuilder().setStatus(SYNC_FAIL.ordinal());
        }
    }

    @Override
    public int version(String appId) {
        return oqsSyncExecutor.version(appId);
    }

    private boolean md5Check(String md5, EntityClassSyncRspProto entityClassSyncRspProto) {
        if (null == md5 || md5.isEmpty() || null == entityClassSyncRspProto) {
            return false;
        }
        return md5.equals(getMD5(entityClassSyncRspProto.toByteArray()));
    }
}
