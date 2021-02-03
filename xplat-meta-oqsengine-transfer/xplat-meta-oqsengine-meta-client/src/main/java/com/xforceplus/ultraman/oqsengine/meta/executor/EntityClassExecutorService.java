package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncReqProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.executor.outter.OqsSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.RequestStatus.SYNC_FAIL;
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
    public EntityClassSyncRequest execute(EntityClassSyncResponse entityClassSyncResponse) {

        EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();
        AtomicInteger index = new AtomicInteger(0);

        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(
                (CompletableFuture<Boolean>[]) entityClassSyncResponse.getEntityClassSyncRspProtoCheckList().stream().map(
                        rspProto -> {
                            return async(() -> {
                                /**
                                 * 该方法返回的错误不会导致重新连接、但会通知服务端本次推送更新失败
                                 */
                                int status = SYNC_FAIL.ordinal();
                                try {
                                    /**
                                     * md5 check && 是否已存在版本判断
                                     */
                                    if (md5Check(rspProto.getMd5(), rspProto.getEntityClassSyncRspProto())
                                            && versionCheck(rspProto.getEntityClassSyncRspProto().getAppId()
                                            , rspProto.getEntityClassSyncRspProto().getVersion())) {

                                        /**
                                         * 执行外部传入的执行器
                                         */
                                        status = oqsSyncExecutor.sync(rspProto.getEntityClassSyncRspProto()) ?
                                                RequestStatus.SYNC_OK.ordinal() : SYNC_FAIL.ordinal();
                                    }
                                } catch (Exception e) {
                                    logger.warn("handle entityClassSyncResponse failed, message : {}", e.getMessage());
                                    status = SYNC_FAIL.ordinal();
                                }

                                builder.setEntityClassSyncReqProtos(index.getAndIncrement(), EntityClassSyncReqProto.newBuilder()
                                        .setAppId(rspProto.getEntityClassSyncRspProto().getAppId())
                                        .setVersion(rspProto.getEntityClassSyncRspProto().getVersion())
                                        .setStatus(status).build());

                                return true;
                            });
                        }).toArray());

        combinedFuture.join();

        return builder.build();
    }

    @Override
    public int version(String appId) {
        return oqsSyncExecutor.version(appId);
    }

    private boolean versionCheck(String appId, int rspVersion) {
        return rspVersion > version(appId);
    }

    private boolean md5Check(String md5, EntityClassSyncRspProto entityClassSyncRspProto) {
        return md5.equals(getMD5(entityClassSyncRspProto.toByteArray()));
    }
}
