package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Resource;

/**
 * desc :
 * name : EntityClassSyncServer
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class EntityClassSyncServer extends EntityClassSyncGrpc.EntityClassSyncImplBase implements IBasicSyncExecutor {

    private Logger logger = LoggerFactory.getLogger(EntityClassSyncServer.class);

    @Resource
    private SyncResponseHandler responseHandler;

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
                responseHandler.onNext(entityClassSyncRequest, responseStreamObserver);
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
