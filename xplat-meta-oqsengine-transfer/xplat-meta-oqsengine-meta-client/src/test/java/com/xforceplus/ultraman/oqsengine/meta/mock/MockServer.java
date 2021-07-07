package com.xforceplus.ultraman.oqsengine.meta.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;

/**
 * desc :
 * name : MockServer
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
public class MockServer extends EntityClassSyncGrpc.EntityClassSyncImplBase {

    private Logger logger = LoggerFactory.getLogger(MockServer.class);

    public volatile static boolean isTestOk = true;

    @Override
    public StreamObserver<EntityClassSyncRequest> register(StreamObserver<EntityClassSyncResponse> responseStreamObserver) {

        return new StreamObserver<EntityClassSyncRequest>() {
            @Override
            public void onNext(EntityClassSyncRequest entityClassSyncRequest) {
                String uid = entityClassSyncRequest.getUid();

                if (entityClassSyncRequest.getStatus() == HEARTBEAT.ordinal()) {
                    /**
                     * 处理心跳
                     */
                    if (isTestOk) {
                        EntityClassSyncResponse.Builder builder = EntityClassSyncResponse.newBuilder().setUid(uid)
                                .setStatus(HEARTBEAT.ordinal());
                        responseStreamObserver.onNext(builder.build());
                    }
                } else if (entityClassSyncRequest.getStatus() == REGISTER.ordinal()) {
                    /**
                     * 处理注册
                     */
                    if (isTestOk) {
                        EntityClassSyncResponse.Builder builder = EntityClassSyncResponse.newBuilder().setUid(uid)
                                .setStatus(REGISTER_OK.ordinal())
                                .setAppId(entityClassSyncRequest.getAppId())
                                .setVersion(entityClassSyncRequest.getVersion());

                        responseStreamObserver.onNext(builder.build());
                    }
                } else if (entityClassSyncRequest.getStatus() == SYNC_OK.ordinal()) {
                    /**
                     * 处理返回结果成功
                     */
                } else if (entityClassSyncRequest.getStatus() == SYNC_FAIL.ordinal()) {
                    /**
                     * 处理返回结果失败
                     */
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
