package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * desc :
 * name : MockerSyncClient
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class MockerSyncClient {

    @Resource
    private MockClient mockClient;

    private Map<String, WatchElement> watchElementMap = new HashMap<>();

    public void start(String host, int port) {
        mockClient.start(host, port);
    }

    public void stop() {
        mockClient.stop();
    }

    /**
     * 初始化stream实现方法
     */
    public StreamObserver<EntityClassSyncRequest> responseEvent() {
        return mockClient.channelStub().register(new StreamObserver<EntityClassSyncResponse>() {
            @Override
            public void onNext(EntityClassSyncResponse entityClassSyncResponse) {

                /**
                 * 更新状态
                 */
                if (entityClassSyncResponse.getStatus() == RequestStatus.REGISTER_OK.ordinal()) {
                    WatchElement w = new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed);
                    watchElementMap.put(w.getAppId(), w);
                } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {

                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
            }
        });
    }
}
