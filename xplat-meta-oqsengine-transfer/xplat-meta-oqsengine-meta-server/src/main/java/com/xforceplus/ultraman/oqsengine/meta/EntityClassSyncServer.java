package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.handler.IResponseHandler;
import io.grpc.stub.StreamObserver;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * entity sync server.
 *
 * @author xujia
 * @since 1.8
 */
public class EntityClassSyncServer extends EntityClassSyncGrpc.EntityClassSyncImplBase implements IBasicSyncExecutor {

    private final Logger logger = LoggerFactory.getLogger(EntityClassSyncServer.class);

    @Resource
    private IResponseHandler responseHandler;

    @Override
    public void start() {
        responseHandler.start();
        logger.debug("entityClassSyncServer start.");
    }

    @Override
    public void stop() {
        responseHandler.stop();
        logger.debug("entityClassSyncServer stop.");
    }

    @Override
    public StreamObserver<EntityClassSyncRequest> register(StreamObserver<EntityClassSyncResponse> responseStreamObserver) {

        return new StreamObserver<EntityClassSyncRequest>() {
            @Override
            public void onNext(EntityClassSyncRequest entityClassSyncRequest) {
                logger.info("grpc-request out : clientId : {}， appId : {}, status : {}", entityClassSyncRequest.getClientId(), entityClassSyncRequest.getAppId(),
                    RequestStatus.getInstance(entityClassSyncRequest.getStatus()));
                responseHandler.invoke(entityClassSyncRequest, responseStreamObserver);
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
