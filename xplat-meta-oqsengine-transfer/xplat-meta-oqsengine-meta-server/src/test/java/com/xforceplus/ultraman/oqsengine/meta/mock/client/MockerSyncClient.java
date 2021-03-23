package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;
import org.junit.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;

/**
 * desc :
 * name : MockerSyncClient
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class MockerSyncClient {

    private MockClient mockClient;

    private Map<String, WatchElement> watchElementMap = new LinkedHashMap<>();

    public Map<String, WatchElement> success = new LinkedHashMap<>();


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

                System.out.println("entityClassSyncResponse : " + entityClassSyncResponse.toString());

                if (entityClassSyncResponse.getStatus() == RequestStatus.REGISTER_OK.ordinal()) {
                    WatchElement w = new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), Confirmed);
                    watchElementMap.put(w.getAppId(), w);
                } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {
                    Assert.assertEquals(entityClassSyncResponse.getMd5(),
                            getMD5(entityClassSyncResponse.getEntityClassSyncRspProto().toByteArray()));
                    WatchElement w = new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), Confirmed);

                    success.put(entityClassSyncResponse.getAppId(), w);
                } else {
                    Assert.assertEquals(entityClassSyncResponse.getStatus(), RequestStatus.HEARTBEAT.ordinal());
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

    public Map<String, WatchElement> getWatchElementMap() {
        return watchElementMap;
    }

    public WatchElement getSuccess(String id) {
        return success.get(id);
    }

    public void releaseSuccess(String id) {
        success.remove(id);
    }
}
