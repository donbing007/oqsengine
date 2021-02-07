package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.EntityClassSyncResponseHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Resource;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;

/**
 * desc :
 * name : EntityClassSyncServer
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class EntityClassSyncServer extends EntityClassSyncGrpc.EntityClassSyncImplBase {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncServer.class);

    @Resource
    private IResponseWatchExecutor watchExecutor;

    @Resource
    private EntityClassSyncResponseHandler responseHandler;

    @Override
    public StreamObserver<EntityClassSyncRequest> register(StreamObserver<EntityClassSyncResponse> responseStreamObserver) {

        return new StreamObserver<EntityClassSyncRequest>() {
            @Override
            public void onNext(EntityClassSyncRequest entityClassSyncRequest) {

                if (entityClassSyncRequest.getStatus() == HEARTBEAT.ordinal()) {
                    /**
                     * 处理心跳
                     */
                    String uid = entityClassSyncRequest.getUid();
                    if (null != uid) {
                        watchExecutor.heartBeat(uid);

                        responseHandler.confirmHeartBeat(uid);
                    }
                } else if (entityClassSyncRequest.getStatus() == REGISTER.ordinal()) {
                    /**
                     * 处理注册
                     */
                    WatchElement w =
                            new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(), WatchElement.AppStatus.Register);
                    watchExecutor.add(entityClassSyncRequest.getUid(), responseStreamObserver, w);

                    /**
                     * 确认注册信息
                     */
                    responseHandler.confirmRegister(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(),
                            entityClassSyncRequest.getUid());

                } else if (entityClassSyncRequest.getStatus() == SYNC_OK.ordinal()) {
                    /**
                     * 处理返回结果成功
                     */
                    watchExecutor.update(entityClassSyncRequest.getUid(),
                            new WatchElement(entityClassSyncRequest.getAppId(), entityClassSyncRequest.getVersion(),
                                    WatchElement.AppStatus.Confirmed));

                } else if (entityClassSyncRequest.getStatus() == SYNC_FAIL.ordinal()) {
                    /**
                     * 处理返回结果失败
                     */
                    responseHandler.pull(entityClassSyncRequest.getAppId(),
                            entityClassSyncRequest.getVersion(), entityClassSyncRequest.getUid());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn("response terminate onError, message :{}", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("response terminate onCompleted");
                responseStreamObserver.onCompleted();
            }
        };
    }


}
