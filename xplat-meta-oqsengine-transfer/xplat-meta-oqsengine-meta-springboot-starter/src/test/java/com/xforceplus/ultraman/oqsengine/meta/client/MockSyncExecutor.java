package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * desc :
 * name : MockSyncExecutor
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@Component("grpcSyncExecutor")
public class MockSyncExecutor implements SyncExecutor {
    private final Logger logger = LoggerFactory.getLogger(MockSyncExecutor.class);

    public volatile RequestStatus status = RequestStatus.SYNC_OK;

    public Map<String, RequestStatusVersion> requestStatusHashMap = new HashMap<>();

    @Override
    public boolean sync(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        Assertions.assertNotNull(entityClassSyncRspProto);
        try {
            if (status.equals(RequestStatus.DATA_ERROR)) {
                throw new MetaSyncClientException("data error.", false);
            } else if (status.equals(RequestStatus.SYNC_OK)) {
                RequestStatusVersion requestStatusVersion = requestStatusHashMap.get(appId);
                if (null != requestStatusVersion) {
                    Assertions.assertTrue(version > requestStatusVersion.getVersion());
                }
                requestStatusHashMap.put(appId, new RequestStatusVersion(status, version));
                logger.info("sync_ok, appId [{}], version [{}], data [{}]", appId, version, entityClassSyncRspProto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            status = RequestStatus.SYNC_OK;
        }

        return true;
    }

    @Override
    public int version(String appId) {
        RequestStatusVersion requestStatusVersion = requestStatusHashMap.get(appId);
        return requestStatusVersion != null ? requestStatusVersion.getVersion() : 0;
    }

    public static class RequestStatusVersion {
        private RequestStatus requestStatus;
        private int version;

        public RequestStatusVersion(RequestStatus requestStatus, int version) {
            this.requestStatus = requestStatus;
            this.version = version;
        }

        public RequestStatus getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(RequestStatus requestStatus) {
            this.requestStatus = requestStatus;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }
}
