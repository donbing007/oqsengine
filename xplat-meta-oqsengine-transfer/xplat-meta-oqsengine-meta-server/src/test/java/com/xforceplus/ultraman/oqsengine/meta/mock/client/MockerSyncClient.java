package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;
import org.junit.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Resource
    private MockClient mockClient;

    private Map<String, WatchElement> watchElementMap = new LinkedHashMap<>();

    public WatchElement success;


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
                            entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed);
                    watchElementMap.put(w.getAppId(), w);
                } else if (entityClassSyncResponse.getStatus() == RequestStatus.SYNC.ordinal()) {
                    Assert.assertEquals(entityClassSyncResponse.getMd5(),
                            getMD5(entityClassSyncResponse.getEntityClassSyncRspProto().toByteArray()));
                    success = new WatchElement(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getEnv(),
                            entityClassSyncResponse.getVersion(), WatchElement.AppStatus.Confirmed);
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

    public WatchElement getSuccess() {
        return success;
    }

    public void releaseSuccess() {
        success = null;
    }
}
